# 🎙️ Speakly AI - Backend

Backend desarrollado con **Java 21**, **Spring Boot 3** y **Spring AI** para una plataforma de tutoría conversacional de inglés en tiempo real. 

El servicio procesa audios de voz enviados por el usuario, realiza la transcripción automática y genera correcciones gramaticales y respuestas dinámicas utilizando modelos de lenguaje de última generación.

---

## 🚀 Tecnologías Principales

- **Java 21**
- **Spring Boot 4.0.8** (Web, Actuator, DevTools)
- **Spring AI** (Integración con modelos de Audio Transcription y LLM)
- **Groq API / Whisper** (Transcripción de audio de alta velocidad)
- **OpenAI Compatible Models / Groq LLMs** (Generación de feedback conversacional)
- **Springdoc OpenAPI (Swagger 3)** (Documentación interactiva de la API)
- **Lombok**
- **Maven**

---

## 🧠 Flujo de la Conversación

```text
[Cliente] (Audio File) 
   │
   ▼
[POST /api/voice/talk] 
   │
   ├─► 1. Transcripción: Spring AI + Whisper (Groq) -> Texto del usuario
   │
   ├─► 2. Inferencia: Spring AI ChatClient (LLM) -> Corrección gramatical + Pregunta de seguimiento
   │
   ▼
[JSON Response] (userText + tutorReply)
