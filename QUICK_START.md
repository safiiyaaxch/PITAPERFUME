# ⚡ Quick Start Guide - Google AI Integration

## 5-Minute Setup

### Step 1️⃣ Get API Key (2 minutes)
1. Go to: https://aistudio.google.com/app/apikey
2. Click "Create API Key"
3. Copy the key

### Step 2️⃣ Set Environment Variable (1 minute)

**Windows:**
```batch
setx GOOGLE_AI_API_KEY "paste_your_key_here"
```
Then restart your terminal/IDE.

**Linux/Mac:**
```bash
export GOOGLE_AI_API_KEY="paste_your_key_here"
# Add to ~/.bashrc or ~/.zshrc to make permanent
```

### Step 3️⃣ Build & Run (2 minutes)
```bash
cd scentify
mvn clean install
mvn spring-boot:run
```

### Step 4️⃣ Test It! ✅
Visit: http://localhost:8080/customer/quiz/start

---

## 🎯 What You Can Do Now

### Option 1: Web Form (Simple)
```
1. Go to /customer/quiz/start
2. Answer 10 quick questions
3. Click "Get AI Recommendations"
4. Get top 3 perfumes instantly!
```

### Option 2: API (Advanced)
```bash
curl -X POST http://localhost:8080/api/quiz/recommend/hybrid \
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

### Option 3: Postman (Most Convenient)
1. Open Postman
2. Click "Import"
3. Select `Scentify_Quiz_AI_API.postman_collection.json`
4. Click "Collections" → Select endpoint
5. Hit "Send"

---

## 📱 Available Endpoints

**Web:**
- `POST /customer/quiz/submit-with-ai` - Web form with AI
- `POST /customer/quiz/submit-hybrid` - Web form with hybrid

**API (JSON):**
- `POST /api/quiz/recommend/ai` - AI only
- `POST /api/quiz/recommend/hybrid` - AI with fallback (recommended)
- `POST /api/quiz/recommend/traditional` - No AI
- `GET /api/quiz/questions` - Get questions

---

## 🧪 Quick Test

### Test 1: Traditional Scoring (Works immediately)
```
URL: http://localhost:8080/customer/quiz/submit
Method: POST
Params: q1=Floral&q2=Moderate&q3=Daily&...
Result: ✅ (100% works, ~200ms)
```

### Test 2: AI Recommendations (If API key set)
```
URL: http://localhost:8080/api/quiz/recommend/ai
Method: POST
Body: {"q1":"Floral","q2":"Moderate",...}
Result: ✅ (Works with key, ~3-5s)
```

### Test 3: Hybrid (Best option)
```
URL: http://localhost:8080/api/quiz/recommend/hybrid
Method: POST
Body: {"q1":"Floral","q2":"Moderate",...}
Result: ✅ (Always works, uses AI if available)
```

---

## 🐛 Troubleshooting

### Can't set environment variable?
→ Edit `application.properties` instead:
```properties
google.ai.api-key=your_key_here
```

### Build fails?
```bash
mvn clean install -U
```

### AI not working but traditional works?
→ Your API key might be wrong. Check:
```bash
echo %GOOGLE_AI_API_KEY%  (Windows)
echo $GOOGLE_AI_API_KEY   (Linux)
```

### Slow responses?
→ That's normal! AI takes 2-5 seconds.
→ Use traditional if you need speed.

---

## 📊 Recommendation Types

| Type | Speed | Accuracy | AI | Fallback |
|------|-------|----------|----|----- |
| **Traditional** | ⚡ Fast | ⭐⭐⭐ | ❌ | N/A |
| **AI-Only** | 🐢 Slow | ⭐⭐⭐⭐⭐ | ✅ | ❌ |
| **Hybrid** | 🐢 Slow | ⭐⭐⭐⭐⭐ | ✅ | ✅ |

**👍 Use Hybrid for best experience!**

---

## 📚 More Info

**Need more details?**
- Setup guide: `GOOGLE_AI_INTEGRATION_GUIDE.md`
- API reference: `API_REFERENCE_GUIDE.md`
- Implementation: `IMPLEMENTATION_SUMMARY.md`

**Want to customize?**
- Edit AI prompt: `GoogleAIService.buildAIPrompt()`
- Change model: `application.properties`
- Add features: `GoogleAIService.java`

---

## 🚀 Next Steps

1. ✅ Set API key
2. ✅ Rebuild project
3. ✅ Test traditional quiz
4. ✅ Test AI quiz
5. ✅ Customize as needed

---

## 💬 That's it!

You now have Google Gemini AI powering your Scentify quiz! 🎉

**Happy perfume recommending!** 🌸✨

---

*Questions?* Check the full guides or Google AI documentation.
