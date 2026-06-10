# 🚀 Scentify AI Quiz Integration - Quick Reference Guide

## API Endpoints

### 1. Get Quiz Questions
```bash
curl -X GET http://localhost:8080/api/quiz/questions
```

**Response:**
```json
{
  "success": true,
  "total": 10,
  "questions": [
    {
      "order": 1,
      "text": "What vibes do you get from flowers and plants?",
      "fieldName": "q1",
      "options": ["Floral", "Fresh", "Woody", "Spicy", "Fruity"]
    },
    ...
  ]
}
```

---

### 2. Get AI-Powered Recommendations
```bash
curl -X POST http://localhost:8080/api/quiz/recommend/ai \
  -H "Content-Type: application/json" \
  -d '{
    "q1": "Floral",
    "q2": "Moderate",
    "q3": "Daily",
    "q4": "Spring",
    "q5": "Feminine",
    "q6": "Citrus",
    "q7": "Vanilla",
    "q8": "Warm",
    "q9": "Slightly sweet",
    "q10": "Natural"
  }'
```

**Response:**
```json
{
  "success": true,
  "type": "AI-Powered",
  "count": 3,
  "recommendations": [
    {
      "productId": "P001",
      "productName": "Lavender Dreams",
      "fragranceFamily": "Floral",
      "intensity": "Moderate",
      "occasion": "Daily",
      "season": "Spring",
      "genderExpression": "Feminine",
      "price": 79.99,
      "stock": 100,
      "description": "A soothing lavender fragrance..."
    },
    ...
  ]
}
```

---

### 3. Get Hybrid Recommendations (AI with Fallback)
```bash
curl -X POST http://localhost:8080/api/quiz/recommend/hybrid \
  -H "Content-Type: application/json" \
  -d '{
    "q1": "Woody",
    "q2": "Strong",
    "q3": "Evening",
    "q4": "Fall",
    "q5": "Masculine",
    "q6": "Spicy",
    "q7": "Woody",
    "q8": "Warm",
    "q9": "Not sweet",
    "q10": "Creative"
  }'
```

---

### 4. Get Traditional Recommendations (No AI)
```bash
curl -X POST http://localhost:8080/api/quiz/recommend/traditional \
  -H "Content-Type: application/json" \
  -d '{
    "q1": "Fruity",
    "q2": "Light",
    "q3": "Active",
    "q4": "Summer",
    "q5": "Unisex",
    "q6": "Fruity",
    "q7": "Clean",
    "q8": "Cool",
    "q9": "Moderately sweet",
    "q10": "Natural"
  }'
```

---

## Web Form Endpoints

### Quiz Flow via Browser

**1. Start Quiz:**
```
GET /customer/quiz/start
```

**2. View Questions:**
```
GET /customer/quiz/questions
```

**3. Submit Quiz - Traditional:**
```
POST /customer/quiz/submit
Form Data:
  q1=Floral&q2=Moderate&q3=Daily&q4=Spring&q5=Feminine&q6=Citrus&q7=Vanilla&q8=Warm&q9=Slightly+sweet&q10=Natural
```

**4. Submit Quiz - With AI:**
```
POST /customer/quiz/submit-with-ai
Form Data: (same as above)
```

**5. Submit Quiz - Hybrid:**
```
POST /customer/quiz/submit-hybrid
Form Data: (same as above)
```

**6. View Results:**
```
Returns: /customer/manage-preference/quiz-results
```

---

## Complete Example with Python

```python
import requests
import json

BASE_URL = "http://localhost:8080/api/quiz"

# Step 1: Get quiz questions
questions_response = requests.get(f"{BASE_URL}/questions")
print("Quiz Questions:", json.dumps(questions_response.json(), indent=2))

# Step 2: Simulate user answers
user_answers = {
    "q1": "Floral",
    "q2": "Moderate",
    "q3": "Daily",
    "q4": "Spring",
    "q5": "Feminine",
    "q6": "Citrus",
    "q7": "Vanilla",
    "q8": "Warm",
    "q9": "Slightly sweet",
    "q10": "Natural"
}

# Step 3: Get AI recommendations
ai_response = requests.post(
    f"{BASE_URL}/recommend/ai",
    json=user_answers,
    headers={"Content-Type": "application/json"}
)
print("\nAI Recommendations:", json.dumps(ai_response.json(), indent=2))

# Step 4: Get Hybrid recommendations (fallback enabled)
hybrid_response = requests.post(
    f"{BASE_URL}/recommend/hybrid",
    json=user_answers,
    headers={"Content-Type": "application/json"}
)
print("\nHybrid Recommendations:", json.dumps(hybrid_response.json(), indent=2))
```

---

## Complete Example with JavaScript/fetch

```javascript
const BASE_URL = "http://localhost:8080/api/quiz";

// Get quiz questions
async function getQuizQuestions() {
    try {
        const response = await fetch(`${BASE_URL}/questions`);
        const data = await response.json();
        console.log("Quiz Questions:", data);
        return data.questions;
    } catch (error) {
        console.error("Error:", error);
    }
}

// Get AI recommendations
async function getAIRecommendations(answers) {
    try {
        const response = await fetch(`${BASE_URL}/recommend/ai`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(answers)
        });
        const data = await response.json();
        console.log("AI Recommendations:", data);
        return data.recommendations;
    } catch (error) {
        console.error("Error:", error);
    }
}

// Usage
const userAnswers = {
    q1: "Floral",
    q2: "Moderate",
    q3: "Daily",
    q4: "Spring",
    q5: "Feminine",
    q6: "Citrus",
    q7: "Vanilla",
    q8: "Warm",
    q9: "Slightly sweet",
    q10: "Natural"
};

getQuizQuestions();
getAIRecommendations(userAnswers);
```

---

## Testing Checklist

- [ ] API key is set in environment variable
- [ ] Application started without errors
- [ ] Can fetch quiz questions via API
- [ ] Can submit traditional quiz
- [ ] Can submit AI-powered quiz
- [ ] Can submit hybrid quiz
- [ ] AI response contains 3 perfumes
- [ ] Recommendations match customer preferences
- [ ] Fallback works if API fails
- [ ] All logs appear in console

---

## Sample Quiz Answers (Test Cases)

### Test 1: Floral Lover
```json
{
  "q1": "Floral",
  "q2": "Light",
  "q3": "Daily",
  "q4": "Spring",
  "q5": "Feminine",
  "q6": "Floral",
  "q7": "Vanilla",
  "q8": "Cool",
  "q9": "Slightly sweet",
  "q10": "Natural"
}
```

### Test 2: Woody Enthusiast
```json
{
  "q1": "Woody",
  "q2": "Strong",
  "q3": "Evening",
  "q4": "Fall",
  "q5": "Masculine",
  "q6": "Spicy",
  "q7": "Woody",
  "q8": "Warm",
  "q9": "Not sweet",
  "q10": "Creative"
}
```

### Test 3: Fresh & Fruity
```json
{
  "q1": "Fruity",
  "q2": "Moderate",
  "q3": "Active",
  "q4": "Summer",
  "q5": "Unisex",
  "q6": "Citrus",
  "q7": "Clean",
  "q8": "Cool",
  "q9": "Moderately sweet",
  "q10": "Natural"
}
```

---

## Environment Setup

### Linux/Mac
```bash
export GOOGLE_AI_API_KEY="your_api_key_here"
mvn spring-boot:run
```

### Windows (CMD)
```batch
setx GOOGLE_AI_API_KEY "your_api_key_here"
mvn spring-boot:run
```

### Windows (PowerShell)
```powershell
$env:GOOGLE_AI_API_KEY = "your_api_key_here"
mvn spring-boot:run
```

---

## Common Response Codes

| Code | Meaning |
|------|---------|
| 200 | Success - recommendations returned |
| 400 | Bad Request - missing or invalid answers |
| 500 | Server Error - API key issue or processing error |

---

## Debugging Tips

### Check logs for AI processing:
```
grep "🤖" logs/app.log    # AI requests
grep "📨" logs/app.log    # AI responses
grep "❌" logs/app.log    # Errors
```

### Test API key directly:
```bash
curl -X GET "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=YOUR_API_KEY"
```

### Test with minimum quiz answers:
```json
{
  "q1": "Floral",
  "q2": "Moderate",
  "q3": "Daily",
  "q4": "Spring",
  "q5": "Feminine",
  "q6": "Citrus",
  "q7": "Vanilla",
  "q8": "Warm",
  "q9": "Slightly sweet",
  "q10": "Natural"
}
```

---

## Performance Metrics

| Operation | Time | Notes |
|-----------|------|-------|
| Get Quiz Questions | < 100ms | Cached |
| Traditional Recommendations | 100-200ms | No API calls |
| AI Recommendations | 2-5 seconds | Depends on Gemini API |
| Hybrid (success) | 2-5 seconds | Same as AI |
| Hybrid (fallback) | 100-200ms | Falls back to traditional |

---

## Next Steps

1. Set up Google AI API key
2. Start the application
3. Test API endpoints with curl or Postman
4. Integrate into frontend UI
5. Monitor performance in logs
6. Adjust prompts as needed based on results

---

## Support URLs

- [Google AI Studio](https://aistudio.google.com/app/apikey)
- [Gemini API Docs](https://ai.google.dev/docs)
- [Spring Boot REST Docs](https://spring.io/guides/gs/rest-service/)

Happy testing! 🎉
