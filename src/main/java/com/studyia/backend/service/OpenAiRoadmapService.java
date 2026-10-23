package com.studyia.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.studyia.backend.model.Student;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.time.Duration;

@Service
public class OpenAiRoadmapService implements AiRoadmapService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiRoadmapService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public OpenAiRoadmapService(RestClient.Builder restClientBuilder, ObjectMapper objectMapper,
                                @Value("${studyia.ai.base-url}") String baseUrl,
                                @Value("${studyia.ai.api-key}") String apiKey,
                                @Value("${studyia.ai.model}") String model) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(15));
        requestFactory.setReadTimeout(Duration.ofSeconds(120));
        this.restClient = restClientBuilder.requestFactory(requestFactory).baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public AiRoadmapResult generateRoadmap(Student student) {
        if (apiKey == null || apiKey.isBlank()) {
            return localDevelopmentResult(student);
        }

        try {
            JsonNode roadmap = generateSegmentedRoadmap(student, null);
            String normalizedContent = ensureFinalMonthIsProject(roadmap, student.getDurationMonths());
            return new AiRoadmapResult(
                    "Roadmap para " + student.getProfessionalGoals(),
                    "Plan de aprendizaje generado por meses para evitar respuestas incompletas",
                    normalizedContent);
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo generar el roadmap con el proveedor de IA", exception);
        }
    }

    @Override
    public AiRoadmapResult generateRoadmap(Student student, String assessmentSummary) {
        if (apiKey == null || apiKey.isBlank()) {
            return localDevelopmentResult(student, assessmentSummary);
        }

        try {
            JsonNode roadmap = generateSegmentedRoadmap(student, assessmentSummary);
            String normalizedContent = ensureFinalMonthIsProject(roadmap, student.getDurationMonths());
            return new AiRoadmapResult(
                    "Ruta ajustada para " + student.getName(),
                    "Plan de aprendizaje ajustado con base en el diagnóstico",
                    normalizedContent);
        } catch (Exception exception) {
            logger.error("Error de OpenAI generando roadmap con examen", exception);
            throw new IllegalStateException("No se pudo generar el roadmap con base en el examen", exception);
        }
    }

    private JsonNode generateSegmentedRoadmap(Student student, String assessmentSummary) throws Exception {
        ObjectNode roadmap = objectMapper.createObjectNode();
        roadmap.put("title", "Roadmap personalizado");
        roadmap.put("summary", "Plan de aprendizaje organizado por meses");
        ArrayNode months = roadmap.putArray("months");
        for (int month = 1; month <= student.getDurationMonths(); month++) {
            months.add(requestMonth(student, month, assessmentSummary));
        }
        return roadmap;
    }

    private JsonNode requestMonth(Student student, int month, String assessmentSummary) throws Exception {
        String context = assessmentSummary == null || assessmentSummary.isBlank()
                ? ""
                : "\nDiagnóstico del estudiante: " + assessmentSummary;
        String prompt = studentPrompt(student) + context
                + "\nGenera únicamente el mes " + month + " de " + student.getDurationMonths() + "."
                + (month == student.getDurationMonths()
                        ? " Es el mes final y debe ser un proyecto integrador."
                        : " Es un mes de teoría, práctica guiada, ejercicio aplicado y verificación.")
                + " Devuelve un único objeto JSON con month, title, objective, outcome, topics y resources."
                + " topics debe tener exactamente 5 objetos con title, instruction, expectedResult y estimatedMinutes."
                + " resources debe tener exactamente 4 objetos con title, url, type y description."
                + " Usa textos breves y no agregues campos, semanas ni explicaciones fuera del JSON.";
        String response = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(Map.of(
                        "model", model,
                        "max_tokens", 1900,
                        "response_format", Map.of("type", "json_object"),
                        "messages", List.of(
                                Map.of("role", "system", "content", segmentedMonthSystemPrompt()),
                                Map.of("role", "user", "content", prompt))))
                .retrieve()
                .body(String.class);
        JsonNode root = objectMapper.readTree(response);
        if (!"stop".equals(root.path("choices").path(0).path("finish_reason").asText())) {
            throw new IllegalStateException("La IA no terminó de generar el mes " + month
                    + ". Inténtalo de nuevo.");
        }
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode() || content.asText().isBlank()) {
            throw new IllegalStateException("La IA no devolvió contenido para el mes " + month);
        }
        JsonNode monthNode = objectMapper.readTree(content.asText());
        if (!monthNode.isObject() || !monthNode.path("topics").isArray()
                || !monthNode.path("resources").isArray()
                || monthNode.path("topics").size() != 5
                || monthNode.path("resources").size() != 4) {
            throw new IllegalStateException("La IA devolvió un formato inválido para el mes " + month);
        }
        return monthNode;
    }

    private String segmentedMonthSystemPrompt() {
        return "Eres un diseñador de planes de estudio. Responde únicamente un objeto JSON válido "
                + "para un solo mes. No uses una clave months, no generes semanas y no escribas texto fuera del JSON. "
                + "Incluye exactamente 5 topics y exactamente 4 resources. Mantén todos los textos breves.";
    }

    @Override
    public AiAssessmentResult generateAssessment(Student student) {
        if (apiKey == null || apiKey.isBlank()) {
            return localDevelopmentAssessment(student);
        }

        try {
            String response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(Map.of(
                            "model", model,
                            "max_tokens", 5000,
                            "response_format", Map.of("type", "json_object"),
                            "messages", List.of(
                                    Map.of("role", "system", "content", assessmentSystemPrompt()),
                                    Map.of("role", "user", "content", assessmentPrompt(student)))))
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            if (!"stop".equals(root.path("choices").path(0).path("finish_reason").asText())) {
                throw new IllegalStateException("La IA no terminó de generar el diagnóstico. Inténtalo de nuevo.");
            }
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new IllegalStateException("La respuesta de IA no contiene contenido");
            }

            JsonNode assessment = objectMapper.readTree(content.asText());
            JsonNode questionsNode = assessment.path("questions");
            List<AssessmentQuestion> questions = new ArrayList<>();
            Map<Integer, String> correctAnswers = new HashMap<>();

            if (questionsNode.isArray()) {
                for (JsonNode questionNode : questionsNode) {
                    Integer questionId = questionNode.path("id").asInt();
                    String questionText = questionNode.path("text").asText();
                    List<AssessmentOption> options = new ArrayList<>();
                    for (JsonNode optionNode : questionNode.path("options")) {
                        options.add(new AssessmentOption(optionNode.path("id").asText(), optionNode.path("text").asText()));
                    }
                    if (questionId > 0 && !questionText.isBlank() && options.size() == 4
                            && !questionNode.path("correctAnswer").asText().isBlank()) {
                        questions.add(new AssessmentQuestion(questionId, questionText, options));
                        correctAnswers.put(questionId, questionNode.path("correctAnswer").asText());
                    }
                }
            }

            if (questions.size() != 20) {
                throw new IllegalStateException("La IA devolvió " + questions.size()
                        + " preguntas válidas; se esperaban 20");
            }

            return new AiAssessmentResult(questions, correctAnswers);
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo generar el examen con el proveedor de IA", exception);
        }
    }

    private String roadmapSystemPrompt() {
        return "Genera un roadmap educativo realista y accionable. Responde solo JSON con las claves "
            + "title, summary, level y months. level debe ser beginner, intermediate o advanced. "
            + "months debe ser una lista con exactamente duración_en_meses elementos, numerados con month. "
            + "Cada mes debe tener title, objective, outcome, topics y resources. "
            + "Los meses 1 a duración_en_meses-1 deben seguir cuatro pasos: teoría, práctica guiada, ejercicio aplicado y verificación. "
            + "topics debe contener exactamente 5 objetos con title, instruction, expectedResult y estimatedMinutes. "
            + "Usa textos breves: instruction y expectedResult deben tener máximo 12 palabras cada uno. "
            + "resources debe contener exactamente 4 objetos por mes: documentación oficial, guía estructurada, práctica y referencia adicional. "
            + "Cada recurso debe incluir title, url, type y una description de máximo 16 palabras. "
            + "Los meses 1 a duración_en_meses-1 deben centrarse en estudio, fundamentos y práctica guiada. "
            + "El último mes debe estar dedicado principalmente a proyectos integradores: construir, probar, documentar y presentar entregables; "
            + "sus topics deben ser fases de proyecto con entregables concretos, no una lista normal de teoría. "
            + "Las URLs deben ser completas, públicas y relevantes para el tema; no inventes URLs. "
            + "No generes semanas ni meses adicionales.";
    }

    private String assessmentSystemPrompt() {
        return "Genera un diagnóstico técnico en formato JSON. Debes devolver un objeto con la clave 'questions', "
            + "que es un array de exactamente 20 preguntas de opción múltiple (A, B, C, D). Cada pregunta "
            + "debe incluir id, text, options (cada opción con id y text) y correctAnswer. "
                + "Evalúa la ruta elegida, intereses y objetivo para medir las habilidades reales; no pidas al estudiante que declare su nivel. "
                + "Las preguntas deben ser tipo examen técnico, basadas en escenarios, lectura de código, decisiones de arquitectura, "
                + "depuración y trade-offs. Incluye distractores plausibles: evita opciones absurdas, obvias, más largas por defecto o que "
                + "repitan palabras de la pregunta. Introduce casos con una mejor respuesta y alternativas parcialmente correctas, para exigir "
                + "criterio y comprensión, no memorización.";
    }

    private String studentPrompt(Student student) {
        return "Perfil del estudiante: nombre=" + student.getName()
            + ", ruta elegida=" + student.getLearningPath()
                + ", horas semanales=" + student.getWeeklyAvailabilityHours()
                + ", duración en meses=" + student.getDurationMonths()
                + ", objetivos profesionales=" + student.getProfessionalGoals();
    }

    private String assessmentPrompt(Student student) {
        return "Genera exactamente 20 preguntas técnicas con nivel apropiado para este estudiante. "
                + "Objetivo profesional: " + student.getProfessionalGoals() + ". "
                + "Ruta elegida: " + student.getLearningPath() + ". "
                + "Tiempo semanal: " + student.getWeeklyAvailabilityHours() + " horas. "
                + "Duración: " + student.getDurationMonths() + " meses.";
    }

    private AiRoadmapResult localDevelopmentResult(Student student) {
        String content = localWeeks(student);
        return new AiRoadmapResult("Roadmap para " + student.getProfessionalGoals(),
                "Plan inicial generado en modo desarrollo", content);
    }

    private AiRoadmapResult localDevelopmentResult(Student student, String assessmentSummary) {
        String content = localWeeks(student);
        return new AiRoadmapResult("Ruta ajustada para " + student.getName(),
                "Plan ajustado con base en el diagnóstico: " + assessmentSummary, content);
    }

    private String localWeeks(Student student) {
        StringBuilder content = new StringBuilder("{\"months\":[");
        int totalMonths = student.getDurationMonths();
        for (int month = 1; month <= totalMonths; month++) {
            if (month > 1) {
                content.append(',');
            }
            boolean projectMonth = month == totalMonths;
            content.append("{\"month\":").append(month)
                    .append(",\"title\":\"")
                    .append(projectMonth ? "Proyecto integrador final" : "Fundamentos y práctica del mes ")
                    .append(projectMonth ? "" : month)
                    .append("\",\"objective\":\"")
                    .append(projectMonth
                            ? "Construir, probar, documentar y presentar un proyecto demostrable de "
                                    + student.getLearningPath()
                            : "Estudiar y practicar progresivamente " + student.getLearningPath())
                    .append("\",\"outcome\":\"")
                    .append(projectMonth
                            ? "Un proyecto funcional, probado, documentado y listo para mostrar"
                            : "Una evidencia práctica de dominio de los conceptos del mes")
                    .append("\",\"studyPlan\":\"")
                    .append(projectMonth
                            ? "Planificar, construir, probar, documentar y presentar"
                            : "Estudiar la teoría, practicar con guía, aplicar y verificar")
                    .append("\",\"topics\":[")
                    .append(projectMonth
                            ? "{\"title\":\"Definir el alcance\",\"instruction\":\"Especifica problema, usuarios, funcionalidades y entregables del proyecto.\",\"expectedResult\":\"Un documento breve de alcance y criterios de éxito\",\"estimatedMinutes\":90},"
                                    + "{\"title\":\"Construir la solución\",\"instruction\":\"Implementa el proyecto por incrementos pequeños y verificables.\",\"expectedResult\":\"Una primera versión funcional\",\"estimatedMinutes\":180},"
                                    + "{\"title\":\"Probar y corregir\",\"instruction\":\"Ejecuta pruebas, corrige errores y valida los casos principales.\",\"expectedResult\":\"Pruebas ejecutadas y errores críticos resueltos\",\"estimatedMinutes\":120},"
                                    + "{\"title\":\"Documentar y presentar\",\"instruction\":\"Explica decisiones, instalación, uso y resultados del proyecto.\",\"expectedResult\":\"README completo y demo preparada\",\"estimatedMinutes\":90}"
                            : "{\"title\":\"Estudiar conceptos clave\",\"instruction\":\"Lee y resume los conceptos fundamentales de "
                                    + student.getLearningPath() + " del mes.\",\"expectedResult\":\"Notas propias con ejemplos\",\"estimatedMinutes\":90},"
                                    + "{\"title\":\"Resolver práctica guiada\",\"instruction\":\"Sigue una práctica paso a paso y reproduce cada resultado.\",\"expectedResult\":\"Ejercicio guiado terminado\",\"estimatedMinutes\":90},"
                                    + "{\"title\":\"Aplicar en un ejercicio\",\"instruction\":\"Resuelve un problema nuevo usando lo estudiado sin copiar la solución.\",\"expectedResult\":\"Solución funcional con explicación\",\"estimatedMinutes\":90},"
                                    + "{\"title\":\"Verificar lo aprendido\",\"instruction\":\"Revisa errores, compara alternativas y registra tus conclusiones.\",\"expectedResult\":\"Checklist de dominio y próximos refuerzos\",\"estimatedMinutes\":60},"
                                    + "{\"title\":\"Consolidar el aprendizaje\",\"instruction\":\"Resume lo aprendido y define el siguiente reto práctico.\",\"expectedResult\":\"Plan breve de refuerzo y siguiente reto\",\"estimatedMinutes\":60}")
                    .append("],\"resources\":[");
            content.append("{\"title\":\"Documentación oficial de la ruta\",\"url\":\"https://developer.mozilla.org/\",\"type\":\"DOCUMENTACION\",\"description\":\"Consultar los conceptos necesarios antes de resolver las actividades.\"")
                    .append("},{\"title\":\"Guía estructurada\",\"url\":\"https://www.freecodecamp.org/learn/\",\"type\":\"CURSO\",\"description\":\"Seguir las lecciones relacionadas y practicar cada concepto.\"")
                    .append("},{\"title\":\"Laboratorio práctico\",\"url\":\"https://exercism.org/\",\"type\":\"PRACTICA\",\"description\":\"Resolver ejercicios para comprobar que puedes aplicar la teoría sin guía.\"")
                    .append("},{\"title\":\"Referencia de consulta\",\"url\":\"https://roadmap.sh/\",\"type\":\"REFERENCIA\",\"description\":\"Usar el mapa de temas para conectar conceptos y detectar vacíos.\"")
                    .append("]}");
        }
        return content.append("]}").toString();
    }

    private String ensureFinalMonthIsProject(JsonNode roadmap, int durationMonths) throws Exception {
        JsonNode monthsNode = roadmap.path("months");
        if (!monthsNode.isArray() || monthsNode.size() != durationMonths) {
            throw new IllegalStateException("La IA debe devolver exactamente " + durationMonths + " meses");
        }
        JsonNode finalMonthNode = monthsNode.get(durationMonths - 1);
        if (!finalMonthNode.isObject()) {
            throw new IllegalStateException("El último mes del roadmap no tiene un formato válido");
        }
        ObjectNode finalMonth = (ObjectNode) finalMonthNode;
        finalMonth.put("month", durationMonths);
        finalMonth.put("title", "Proyecto integrador final");
        finalMonth.put("objective", "Construir, probar, documentar y presentar un proyecto demostrable de "
                + "la ruta " + "del estudiante");
        finalMonth.put("outcome", "Un proyecto funcional, probado, documentado y listo para presentar");
        ArrayNode topics = finalMonth.putArray("topics");
        topics.add(objectMapper.createObjectNode()
                .put("title", "Definir el alcance")
                .put("instruction", "Especifica problema, usuarios, funcionalidades y entregables.")
                .put("expectedResult", "Documento de alcance y criterios de éxito")
                .put("estimatedMinutes", 90));
        topics.add(objectMapper.createObjectNode()
                .put("title", "Construir la solución")
                .put("instruction", "Implementa el proyecto por incrementos pequeños y verificables.")
                .put("expectedResult", "Primera versión funcional")
                .put("estimatedMinutes", 180));
        topics.add(objectMapper.createObjectNode()
                .put("title", "Probar y corregir")
                .put("instruction", "Ejecuta pruebas, corrige errores y valida los casos principales.")
                .put("expectedResult", "Pruebas ejecutadas y errores críticos resueltos")
                .put("estimatedMinutes", 120));
        topics.add(objectMapper.createObjectNode()
                .put("title", "Documentar y presentar")
                .put("instruction", "Explica decisiones, instalación, uso y resultados.")
                .put("expectedResult", "README completo y demo preparada")
                .put("estimatedMinutes", 90));
        topics.add(objectMapper.createObjectNode()
                .put("title", "Medir y mejorar")
                .put("instruction", "Evalúa resultados, incorpora mejoras y prepara la versión final.")
                .put("expectedResult", "Proyecto refinado con evidencias de impacto")
                .put("estimatedMinutes", 90));
        return objectMapper.writeValueAsString(roadmap);
    }

    private AiAssessmentResult localDevelopmentAssessment(Student student) {
        String[] questionTexts = {
            "Una API devuelve 500 cuando recibe dos solicitudes simultáneas que actualizan el mismo recurso. ¿Qué cambio ataca mejor la condición de carrera sin bloquear toda la aplicación?",
            "Un endpoint de Spring Boot consulta una relación lazy después de salir de la transacción. ¿Qué solución conserva límites claros de capa y evita depender de open-in-view?",
            "Un cliente reintenta automáticamente una solicitud POST después de un timeout. ¿Qué diseño reduce el riesgo de crear dos órdenes?",
            "Una consulta funciona con 100 filas pero tarda varios segundos con millones. ¿Qué evidencia debería revisarse primero antes de añadir caché?",
            "Una prueba de integración falla solo en CI por diferencias de zona horaria. ¿Qué práctica hace el resultado determinista?",
            "Una aplicación necesita leer eventos en orden por usuario y procesarlos varias veces si un consumidor falla. ¿Qué decisión de diseño es más adecuada?",
            "Un servicio recibe un token JWT válido firmado por la aplicación, pero destinado a otro tipo de operación. ¿Qué validación falta?",
            "Un cambio de base de datos debe convivir con dos versiones del backend durante un despliegue gradual. ¿Qué estrategia reduce el riesgo de incompatibilidad?",
            "Un componente de frontend dispara una petición cada vez que cambia una tecla y el servidor empieza a saturarse. ¿Qué combinación mejora UX y carga?",
            "Un contenedor reinicia constantemente, aunque el proceso principal termina con código 0. ¿Qué hipótesis debe comprobarse primero?",
            "Una clase tiene muchas condiciones anidadas y cada nuevo requisito rompe una prueba existente. ¿Qué refactor mejora extensibilidad sin ocultar reglas de negocio?",
            "Un sistema permite cambiar el correo de una cuenta sin volver a verificarlo. ¿Cuál es el riesgo principal y qué control lo mitiga?",
            "Una consulta necesita filtrar por estado y ordenar por fecha en una tabla grande. ¿Qué debe guiar el diseño del índice?",
            "Un pipeline ejecuta despliegue aunque fallen las pruebas de contrato. ¿Qué relación entre etapas debe configurarse?",
            "Dos servicios actualizan datos relacionados y uno falla después de que el otro confirma. ¿Qué enfoque es preferible si no se necesita una transacción distribuida?",
            "Un endpoint protegido responde igual para un usuario inexistente y uno sin permisos. ¿Qué objetivo de seguridad puede estar persiguiendo esa decisión?",
            "Una aplicación escala horizontalmente y pierde sesiones guardadas en memoria local. ¿Qué cambio elimina esa dependencia?",
            "Un error intermitente no puede reproducirse localmente. ¿Qué instrumentación aporta más contexto sin registrar secretos?",
            "Un equipo quiere cambiar una implementación sin modificar consumidores. ¿Qué propiedad del diseño debe priorizar?",
            "Un recurso aparece como completado en la UI, pero la petición falla. ¿Qué patrón evita mostrar un estado de éxito falso?"
        };
        String[][] optionTexts = {
            {"Usar una variable global para serializar las solicitudes", "Aplicar control de concurrencia optimista y rechazar versiones obsoletas", "Duplicar la tabla para separar lectores y escritores", "Aumentar el timeout del cliente"},
            {"Forzar todas las relaciones a eager", "Devolver entidades JPA directamente desde el controlador", "Cargar explícitamente el agregado necesario dentro del servicio transaccional", "Abrir una sesión global para cada usuario"},
            {"Hacer todos los POST asíncronos", "Usar una clave de idempotencia asociada a la operación", "Aumentar el número de reintentos", "Ocultar el error y continuar"},
            {"Cambiar el lenguaje de programación", "Revisar plan de ejecución, selectividad e índices con datos reales", "Añadir más memoria al navegador", "Convertir la respuesta a XML"},
            {"Fijar la zona horaria en la máquina del desarrollador", "Comparar fechas como texto local", "Usar instantes/zonas explícitas y un reloj controlado en pruebas", "Desactivar las aserciones temporales"},
            {"Un consumidor por partición clave y offsets confirmados tras procesar", "Un thread global sin persistir posición", "Eliminar eventos después de leerlos", "Procesarlos siempre de forma aleatoria"},
            {"Solo comprobar que el token no haya expirado", "Validar también audiencia, tipo de token y permisos requeridos", "Aceptar cualquier token firmado por el servidor", "Renovar el token automáticamente"},
            {"Cambiar y borrar columnas en el mismo despliegue", "Aplicar cambios compatibles primero y retirar lo antiguo después", "Detener todos los servidores hasta terminar", "Mantener dos bases sin sincronización"},
            {"Debounce, cancelación de solicitudes obsoletas y feedback de carga", "Enviar más solicitudes para suavizar la respuesta", "Bloquear la interfaz hasta recibir cada respuesta", "Desactivar la validación del servidor"},
            {"El proceso terminó correctamente pero el healthcheck o comando de arranque no coincide", "La imagen siempre necesita más CPU", "Los logs deben eliminarse", "El contenedor no puede reiniciarse"},
            {"Extraer cada condición a un booleano genérico", "Modelar reglas con estrategias/objetos de política y pruebas enfocadas", "Añadir más if dentro del mismo método", "Copiar la clase para cada caso"},
            {"Solo afecta a la interfaz", "Permite apropiación de cuentas; exigir verificación del nuevo correo", "Evita todos los ataques automáticamente", "Hace innecesario el login"},
            {"El nombre de la columna más larga", "Las columnas usadas juntas en filtro/orden y su selectividad", "El orden alfabético del código", "La cantidad de endpoints"},
            {"Ejecutar ambas etapas sin dependencia", "Hacer que el despliegue dependa de un resultado exitoso de pruebas", "Ignorar el código de salida", "Reintentar el despliegue indefinidamente"},
            {"Usar un workflow con estado y compensaciones", "Bloquear todas las tablas para siempre", "Duplicar cada escritura manualmente", "Eliminar la consistencia del dominio"},
            {"Evitar enumeración de cuentas mediante respuestas equivalentes", "Revelar siempre qué usuario existe", "Desactivar la autorización", "Usar contraseñas más cortas"},
            {"Guardar sesiones únicamente en el proceso local", "Mover el estado a almacenamiento compartido o usar tokens adecuados", "Desactivar el balanceador", "Crear una sesión por endpoint"},
            {"Más logs con headers completos", "Trazas/correlation IDs, métricas y contexto sanitizado", "Registrar contraseñas para comparar", "Eliminar los errores"},
            {"Contratos estables y dependencias invertidas", "Exponer todas las clases internas", "Acoplar consumidores a tablas", "Cambiar nombres en cada release"},
            {"Actualizar la UI antes de enviar la petición", "Esperar confirmación y revertir o informar si falla", "Ocultar el error de red", "Marcar todo como completado localmente"}
        };
        List<AssessmentQuestion> questions = new ArrayList<>();
        Map<Integer, String> correctAnswers = new HashMap<>();
        for (int index = 0; index < questionTexts.length; index++) {
            int questionId = index + 1;
            List<AssessmentOption> options = new ArrayList<>();
            for (String option : optionTexts[index]) {
                options.add(new AssessmentOption(String.valueOf((char) ('A' + options.size())), option));
            }
            questions.add(new AssessmentQuestion(questionId, questionTexts[index], options));
            correctAnswers.put(questionId, new String[]{"B", "C", "B", "B", "C", "A", "B", "B", "A", "A",
                    "B", "B", "B", "B", "A", "A", "B", "B", "A", "B"}[index]);
        }
        return new AiAssessmentResult(questions, correctAnswers);
    }
}
