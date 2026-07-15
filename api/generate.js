export default async function handler(req, res) {
  // Enable CORS
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,PATCH,DELETE,POST,PUT');
  res.setHeader(
    'Access-Control-Allow-Headers',
    'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version'
  );

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  const { niche, mood, customPrompt } = req.body || {};
  const apiKey = process.env.GEMINI_API_KEY;

  if (!apiKey) {
    return res.status(500).json({
      error: 'Gemini API Key is not configured. Please add GEMINI_API_KEY to your Vercel Environment Variables.'
    });
  }

  // Construct prompt for Gemini
  let promptText = `Generate an exceptionally inspiring, beautiful, and authentic quote.
Category/Niche: ${niche || 'General Inspiration'}
Mood/Tone: ${mood || 'Poetic & Deep'}`;

  if (customPrompt && customPrompt.trim()) {
    promptText += `\nAdditional Context/Idea from User: ${customPrompt}`;
  }

  promptText += `\n\nReturn the response as a JSON object containing exactly two keys: "quote" (the text of the quote) and "author" (the author, or a fitting attribution like a historical figure, philosopher, or "Unknown" if appropriate). Do not include any markdown formatting, backticks, or extra text. Output raw JSON only.\nExample:\n{"quote": "The flower that blooms in adversity is the rarest and most beautiful of all.", "author": "Mulan"}`;

  try {
    const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`;
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        contents: [
          {
            parts: [
              { text: promptText }
            ]
          }
        ],
        generationConfig: {
          responseMimeType: "application/json"
        }
      })
    });

    if (!response.ok) {
      const errText = await response.text();
      return res.status(response.status).json({ error: `Gemini API returned error: ${errText}` });
    }

    const data = await response.json();
    const resultText = data.candidates?.[0]?.content?.parts?.[0]?.text;
    
    if (!resultText) {
      return res.status(500).json({ error: 'Empty response received from Gemini AI.' });
    }

    const quoteObj = JSON.parse(resultText.trim());
    return res.status(200).json(quoteObj);
  } catch (error) {
    console.error('Error in API handler:', error);
    return res.status(500).json({ error: 'Internal server error: ' + error.message });
  }
}
