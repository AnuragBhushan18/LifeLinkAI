
---

## Phase 6 — AI/LLM Integration

Phase 6 introduces useful, credible, production-style LLM capabilities while keeping all critical healthcare and emergency decisions deterministic and controlled by the Java backend.

**IMPORTANT:** AI features provide assistance and summarization only. Critical emergency and clinical decisions remain under deterministic application logic and authorized human professionals.

- **Patient AI Healthcare Assistant**: Patients can chat with an AI for general healthcare information, first-aid, and basic emergency guidance. The AI will explicitly refuse to diagnose and will recommend seeking professional care for serious symptoms.
- **Doctor AI Medical Summary**: Authorized doctors can generate a concise AI summary of a patient's relevant medical history, allergies, medications, and clinical notes based strictly on the MongoDB context provided by the backend.
- **Admin AI Analytics Assistant**: Admins can ask natural-language questions about system analytics (e.g., "What is the current number of available beds?"). The LLM uses a controlled tool registry to execute approved backend queries rather than running arbitrary MongoDB queries.

### Architecture & Security
- **Provider-Independent**: Implemented an extensible `LLMProvider` interface (currently using `GeminiProvider` via REST).
- **Data Minimization**: The backend retrieves only the necessary data (`AIContextService`) before passing it to the LLM. 
- **Tool Registry**: `AIToolRegistry` securely maps LLM intent to explicitly approved Java methods.
- **Prompt Injection Protection**: LLMs are constrained by backend authorization; a user cannot bypass Java authorization by manipulating the LLM. 
- **Configuration**: Managed via `.env` / `application.properties` (`LLM_PROVIDER`, `LLM_MODEL`, `LLM_API_KEY`).
