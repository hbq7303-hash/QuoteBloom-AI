document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const form = document.getElementById('quote-form');
    const nicheSelect = document.getElementById('niche-select');
    const moodSelect = document.getElementById('mood-select');
    const customPrompt = document.getElementById('custom-prompt');
    const charCount = document.querySelector('.char-count');
    const generateBtn = document.getElementById('generate-btn');
    const btnText = generateBtn.querySelector('.btn-text');
    const spinnerIcon = generateBtn.querySelector('.spinner-icon');

    const idleState = document.getElementById('quote-idle-state');
    const loadingState = document.getElementById('quote-loading-state');
    const errorState = document.getElementById('quote-error-state');
    const successState = document.getElementById('quote-success-state');
    const errorMessage = document.getElementById('error-message');

    const displayQuoteText = document.getElementById('display-quote-text');
    const displayQuoteAuthor = document.getElementById('display-quote-author');
    const tagNiche = document.getElementById('tag-niche');
    const tagMood = document.getElementById('tag-mood');

    const copyBtn = document.getElementById('copy-btn');
    const shareBtn = document.getElementById('share-btn');
    const retryBtn = document.getElementById('retry-btn');

    // Update char count as user types
    customPrompt.addEventListener('input', () => {
        const length = customPrompt.value.length;
        charCount.textContent = `${length} / 500`;
    });

    // Helper: Switch active result view
    function showState(state) {
        idleState.classList.add('hide');
        loadingState.classList.add('hide');
        errorState.classList.add('hide');
        successState.classList.add('hide');

        if (state === 'idle') idleState.classList.remove('hide');
        else if (state === 'loading') loadingState.classList.remove('hide');
        else if (state === 'error') errorState.classList.remove('hide');
        else if (state === 'success') successState.classList.remove('hide');
    }

    // Call API to generate quote
    async function fetchQuote(niche, mood, prompt) {
        showState('loading');
        generateBtn.disabled = true;
        btnText.textContent = "Blooming...";
        spinnerIcon.classList.remove('hide');

        try {
            const response = await fetch('/api/generate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    niche: niche,
                    mood: mood,
                    customPrompt: prompt
                })
            });

            if (!response.ok) {
                const data = await response.json().catch(() => ({}));
                throw new Error(data.error || `Server responded with status ${response.status}`);
            }

            const data = await response.json();
            
            if (data && data.quote) {
                // Set the success state
                displayQuoteText.textContent = `"${data.quote}"`;
                displayQuoteAuthor.textContent = data.author ? `— ${data.author}` : '— Unknown';
                
                // Update tags
                tagNiche.innerHTML = `<i class="fa-solid fa-tag"></i> ${niche}`;
                tagMood.innerHTML = `<i class="fa-solid fa-heart"></i> ${mood}`;

                showState('success');
            } else {
                throw new Error("Invalid quote format received from Gemini AI.");
            }

        } catch (error) {
            console.error("API error:", error);
            errorMessage.textContent = error.message || "Failed to establish secure connection with Gemini.";
            showState('error');
        } finally {
            generateBtn.disabled = false;
            btnText.textContent = "Bloom My Quote";
            spinnerIcon.classList.add('hide');
        }
    }

    // Form submit listener
    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const niche = nicheSelect.value;
        const mood = moodSelect.value;
        const prompt = customPrompt.value;
        fetchQuote(niche, mood, prompt);
    });

    // Retry listener
    retryBtn.addEventListener('click', () => {
        const niche = nicheSelect.value;
        const mood = moodSelect.value;
        const prompt = customPrompt.value;
        fetchQuote(niche, mood, prompt);
    });

    // Copy to clipboard with UI feedback
    copyBtn.addEventListener('click', async () => {
        const quoteText = displayQuoteText.textContent;
        const quoteAuthor = displayQuoteAuthor.textContent;
        const fullShare = `${quoteText}\n${quoteAuthor}\n\nGenerated with QuoteBloom AI`;

        try {
            await navigator.clipboard.writeText(fullShare);
            const origHTML = copyBtn.innerHTML;
            copyBtn.innerHTML = `<i class="fa-solid fa-check" style="color: var(--success-color)"></i> Copied!`;
            copyBtn.style.borderColor = 'var(--success-color)';
            
            setTimeout(() => {
                copyBtn.innerHTML = origHTML;
                copyBtn.style.borderColor = '';
            }, 2000);
        } catch (err) {
            alert("Failed to copy quote to clipboard.");
        }
    });

    // Native Web Share API integration
    shareBtn.addEventListener('click', async () => {
        const quoteText = displayQuoteText.textContent;
        const quoteAuthor = displayQuoteAuthor.textContent;
        const shareData = {
            title: 'QuoteBloom AI',
            text: `${quoteText} ${quoteAuthor}`,
            url: window.location.href
        };

        if (navigator.share && navigator.canShare && navigator.canShare(shareData)) {
            try {
                await navigator.share(shareData);
            } catch (err) {
                if (err.name !== 'AbortError') {
                    console.error("Share error:", err);
                }
            }
        } else {
            // Fallback for non-supporting browsers
            const twitterUrl = `https://twitter.com/intent/tweet?text=${encodeURIComponent(quoteText + " " + quoteAuthor + " #QuoteBloomAI")}`;
            window.open(twitterUrl, '_blank');
        }
    });
});
