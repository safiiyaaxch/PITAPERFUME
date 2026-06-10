# 🤖 Google AI Studio (Gemini API) Integration Guide for Scentify Quiz Module

## Overview

You now have Google Gemini AI integrated into your Scentify quiz module for intelligent perfume recommendations. The system can recommend the top 3 suitable perfumes based on customer preferences using natural language processing.

## What Was Added

### 1. **New Files Created**
- `GoogleAIService.java` - Handles all Gemini API integration

### 2. **Modified Files**
- `pom.xml` - Added Google Generative AI dependencies
- `application.properties` - Added Google AI configuration
- `QuizService.java` - Added AI recommendation methods
- `QuizController.java` - Added API endpoints for AI recommendations

### 3. **New Capabilities**
- **AI-Powered Recommendations** (`/customer/quiz/submit-with-ai`)
- **Hybrid Recommendations** (`/customer/quiz/submit-hybrid`) - AI with fallback to traditional scoring

---

## 📋 Setup Instructions

### Step 1: Get Your Google AI API Key

1. Go to [Google AI Studio](https://aistudio.google.com/app/apikey)
2. Click on "Create API Key"
3. Choose "Create API key in new project" or use an existing project
4. Copy your API key

### Step 2: Set Environment Variable

**Option A: Windows (Recommended)**
```batch
setx GOOGLE_AI_API_KEY "your_api_key_here"
```
Then restart your IDE/terminal for the change to take effect.

**Option B: Add to `application.properties`**
```properties
google.ai.api-key=your_api_key_here
```
⚠️ **NOT recommended for production** - exposes your API key in version control

**Option C: Use `.env` file with Spring Boot**
1. Create `.env` file in project root:
```
GOOGLE_AI_API_KEY=your_api_key_here
```
2. Add to `application.properties`:
```properties
google.ai.api-key=${GOOGLE_AI_API_KEY}
```

### Step 3: Build and Run

```bash
cd scentify
mvn clean install
mvn spring-boot:run
```

### Step 4: Test the Integration

**Via HTML Form:**
1. Go to Customer Dashboard
2. Click "Find Your Perfect Fragrance" → Start Quiz
3. Complete the quiz and submit

**API Endpoints:**

**Traditional Scoring (Existing):**
```
POST /customer/quiz/submit
Parameters: Quiz answers as form data
```

**AI-Powered Only:**
```
POST /customer/quiz/submit-with-ai
Parameters: Quiz answers as form data
```

**Hybrid (AI with Fallback):**
```
POST /customer/quiz/submit-hybrid
Parameters: Quiz answers as form data
```

---

## 🎯 How It Works

### Traditional Flow
```
Quiz Answers → Calculate Scores → Return Top 3
```

### AI-Enhanced Flow
```
Quiz Answers → Get Products from DB → Send to Gemini AI 
→ AI analyzes preferences & product catalog → Parse AI response 
→ Return Top 3 Recommendations
```

### Hybrid Flow (Recommended)
```
Quiz Answers → Try AI Recommendation 
→ If successful: Return AI recommendations
→ If failed: Fallback to traditional scoring
```

---

## 📝 Quiz Answers Format

The system processes these 10 quiz questions:

| Q# | Question | Field | Options |
|---|---|---|---|
| 1 | Fragrance vibes | q1 | Floral, Fresh, Woody, Spicy, Fruity |
| 2 | Fragrance strength | q2 | Light, Moderate, Strong, Very Strong |
| 3 | Wear occasion | q3 | Daily, Professional, Evening, Active, Versatile |
| 4 | Season preference | q4 | Spring, Summer, Fall, Winter, Year-round |
| 5 | Gender expression | q5 | Feminine, Masculine, Unisex |
| 6 | Top notes preference | q6 | Citrus, Herbal, Fruity, Spicy, Floral |
| 7 | Base notes preference | q7 | Musk, Vanilla, Woody, Patchouli, Clean |
| 8 | Temperature | q8 | Cool, Warm, Balanced |
| 9 | Sweetness level | q9 | Not sweet, Slightly sweet, Moderately sweet, Very sweet, Gourmand |
| 10 | Naturalness | q10 | Natural, Slightly abstract, Creative, No preference |

---

## 🔧 Product Database Requirements

Ensure your Product table has these columns populated:

```java
- productName (required)
- fragranceFamily (required)
- intensity (required)
- occasion (required)
- season (required)
- genderExpression (required)
- topNotes (required)
- baseNotes (required)
- sillage (recommended)
- longevity (recommended)
- sweetness (recommended)
- description (recommended)
- approvalStatus = "approved"
```

**Example:**
```sql
INSERT INTO product VALUES (
    'P001', 1, 'CAT001', 'Lavender Dreams',
    'A soothing lavender fragrance...', 'image.jpg',
    79.99, 100, 'Floral', 'Citrus', 'Floral', 'Lavender',
    'Skin scent', 'Very Long', 'Light',
    'Spring', 'Daily', 'Feminine', 3, 'Natural',
    'approved', NOW(), NOW()
);
```

---

## 🚀 Advanced Configuration

### Change AI Model

Edit `application.properties`:
```properties
# Available models: gemini-pro, gemini-pro-vision, etc.
google.ai.model=gemini-pro
```

### API Limits & Quotas

- **Free Tier**: 60 requests per minute
- **Paid Tier**: Higher limits
- Check [Google AI Pricing](https://ai.google.dev/pricing)

### Customize AI Prompt

Edit the `buildAIPrompt()` method in `GoogleAIService.java` to customize how the AI evaluates perfumes.

---

## 🛠️ Troubleshooting

### 1. **"API key is not configured"**
- Ensure `GOOGLE_AI_API_KEY` environment variable is set
- Verify the API key is valid
- Restart application after setting environment variable

### 2. **"Could not extract text from API response"**
- API key might be incorrect
- Quota might be exceeded
- Check network connectivity

### 3. **"No recommendations from AI"**
- Ensure products exist with `approvalStatus = "approved"`
- Verify product descriptions are populated
- System will fallback to traditional scoring

### 4. **Slow Recommendations**
- AI API calls take 2-3 seconds typically
- Use hybrid approach for resilience
- Consider caching results for frequent queries

### 5. **Maven Dependency Errors**
```bash
mvn clean install -U
```
(Forces update of dependencies)

---

## 📊 Monitoring & Logs

The system logs AI activities:

```
🤖 Sending request to Google Gemini AI...
📡 Calling Gemini API: https://generativelanguage.googleapis.com/v1beta/models/...
📨 AI Response: [first 200 chars]...
✅ AI generated X recommendations
```

Monitor these in your application logs to track AI performance.

---

## 🔒 Security Best Practices

1. **Never commit API keys** to version control
2. **Use environment variables** in production
3. **Implement rate limiting** if needed
4. **Validate all user inputs** before sending to AI
5. **Cache results** to avoid unnecessary API calls

---

## 📱 Frontend Integration (Quiz Results Page)

Your `quiz-results.html` can display recommendation type:

```html
<div th:if="${recommendationType}">
    <badge class="badge bg-info">[[${recommendationType}]]</badge>
</div>
```

---

## 🎓 Example Usage

### Using AI-Powered Recommendations in Your Controller

```java
@PostMapping("/quiz/ai-recommendations")
public String getAIRecommendations(@RequestParam Map<String, String> answers,
                                    Model model) {
    List<Product> recommendations = quizService.getAIPoweredRecommendations(answers);
    model.addAttribute("recommendations", recommendations);
    return "quiz-results";
}
```

### Using Hybrid Approach (Recommended)

```java
@PostMapping("/quiz/smart-recommendations")
public String getSmartRecommendations(@RequestParam Map<String, String> answers,
                                       Model model) {
    // Uses AI with fallback to traditional scoring
    List<Product> recommendations = quizService.getHybridRecommendations(answers);
    model.addAttribute("recommendations", recommendations);
    return "quiz-results";
}
```

---

## 📈 Performance Optimization

### Caching AI Responses

You can implement caching to avoid repeated API calls:

```java
@Service
@Cacheable(value = "recommendations")
public List<Product> getAIPoweredRecommendations(Map<String, String> answers) {
    // Will be cached based on answers
}
```

### Batch Processing

For multiple customers:

```java
List<Map<String, String>> allAnswers = getMultipleQuizzes();
List<List<Product>> allRecommendations = allAnswers.stream()
    .parallel()
    .map(quizService::getAIPoweredRecommendations)
    .collect(Collectors.toList());
```

---

## 🔄 Next Steps

1. ✅ Add Google AI API key
2. ✅ Rebuild and run application
3. ✅ Test quiz with AI recommendations
4. ✅ Monitor logs for any issues
5. ✅ Customize AI prompts as needed
6. ✅ Consider implementing caching for production

---

## 📚 Additional Resources

- [Google AI Studio Documentation](https://ai.google.dev/docs)
- [Gemini API Reference](https://ai.google.dev/api/generate-content)
- [Google AI Pricing](https://ai.google.dev/pricing)
- [Spring Boot Integration Best Practices](https://spring.io/projects/spring-boot)

---

## 💡 Tips & Tricks

1. **Test with curl:**
```bash
curl -X POST http://localhost:8080/customer/quiz/submit-with-ai \
  -d "q1=Floral&q2=Light&q3=Daily" \
  -b "sessionId=your_session"
```

2. **Monitor API usage** in Google AI Studio dashboard

3. **A/B Test** different AI models or prompts

4. **Collect feedback** on AI recommendations for future improvements

---

## Support

For issues or questions:
- Check the troubleshooting section above
- Review application logs
- Verify Google AI API key is valid
- Check product database has required fields

Happy perfume recommending! 🌸✨
