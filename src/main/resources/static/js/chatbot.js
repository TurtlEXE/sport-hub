document.addEventListener('DOMContentLoaded', function () {
    // 1. Inject Widget HTML
    const currentLang = document.documentElement.lang || 'vi';
    let welcomeMsg = "Xin chào! 👋 Tôi là trợ lý ảo AI của Sport Hub. Tôi có thể giúp bạn kiểm tra sân trống, thông tin giá cả, mã giảm giá và quy trình đặt sân. Bạn cần hỗ trợ gì hôm nay?";
    let placeholderMsg = "Hỏi AI về giờ trống, sân bãi...";
    let chatbotTitle = "Trợ Lý AI Sport Hub";
    let chatbotStatus = "Sẵn sàng hỗ trợ 24/7";

    if (currentLang === 'en') {
        welcomeMsg = "Hello! 👋 I am Sport Hub's AI virtual assistant. I can help you check court availability, pricing information, discount codes, and the booking process. How can I help you today?";
        placeholderMsg = "Ask AI about availability, courts...";
        chatbotTitle = "Sport Hub AI Assistant";
        chatbotStatus = "Ready to help 24/7";
    } else if (currentLang === 'mm' || currentLang === 'my') {
        welcomeMsg = "မင်္ဂလာပါ! 👋 ကျွန်ုပ်သည် Sport Hub ၏ AI virtual လက်ထောက်ဖြစ်သည်။ ကွင်းရရှိနိုင်မှု၊ စျေးနှုန်းအချက်အလက်၊ လျှော့စျေးကုဒ်များနှင့် ဘွတ်ကင်လုပ်ငန်းစဉ်များကို စစ်ဆေးရန် သင့်အား ကျွန်ုပ်ကူညီနိုင်ပါသည်။ ဒီနေ့ သင့်ကို ဘယ်လိုကူညီနိုင်မလဲ။";
        placeholderMsg = "ရရှိနိုင်မှုအကြောင်း AI ကို မေးပါ...";
        chatbotTitle = "Sport Hub AI လက်ထောက်";
        chatbotStatus = "24/7 ကူညီပေးရန် အဆင်သင့်ဖြစ်ပါပြီ";
    }
        
    const chatbotContainer = document.createElement('div');
    chatbotContainer.id = 'ai-chatbot-widget';
    chatbotContainer.innerHTML = `
        <!-- Floating Chat Button -->
        <button id="chatbot-toggle-btn" class="fixed bottom-6 right-6 z-50 w-14 h-14 bg-gradient-to-tr from-primary-blue to-blue-500 text-white rounded-full shadow-[0_0_20px_rgba(59,130,246,0.5)] hover:shadow-[0_0_30px_rgba(59,130,246,0.8)] hover:scale-110 active:scale-95 transition-all duration-300 flex items-center justify-center border-2 border-white/20 group">
            <svg id="chatbot-icon-msg" class="w-7 h-7 transition-all duration-300 group-hover:rotate-12 group-hover:scale-110" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <!-- Antenna and Head -->
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 2a2 2 0 012 2v2h3.5a2.5 2.5 0 012.5 2.5v9a2.5 2.5 0 01-2.5 2.5h-11A2.5 2.5 0 014 17.5v-9A2.5 2.5 0 016.5 6H10V4a2 2 0 012-2z"></path>
                <!-- Eyes -->
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9.5 11.5h.01M14.5 11.5h.01"></path>
                <!-- Mouth -->
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 15.5h6"></path>
                <!-- Ears -->
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M2 12h2m16 0h2"></path>
            </svg>
            <svg id="chatbot-icon-close" class="w-6 h-6 hidden transition-transform duration-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
        </button>

        <!-- Chat Panel -->
        <div id="chatbot-panel" class="fixed bottom-24 right-6 z-50 w-96 max-w-[calc(100vw-3rem)] h-[520px] max-h-[75vh] bg-white/95 backdrop-blur-xl rounded-3xl shadow-2xl border border-slate-200/80 flex flex-col transition-all duration-300 transform translate-y-4 opacity-0 pointer-events-none overflow-hidden">
            <!-- Header -->
            <div class="bg-gradient-to-r from-primary-blue to-blue-600 px-6 py-4 text-white flex items-center justify-between shadow-sm">
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center backdrop-blur-md border border-white/30">
                        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 2a2 2 0 012 2v2h3.5a2.5 2.5 0 012.5 2.5v9a2.5 2.5 0 01-2.5 2.5h-11A2.5 2.5 0 014 17.5v-9A2.5 2.5 0 016.5 6H10V4a2 2 0 012-2z"></path>
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9.5 11.5h.01M14.5 11.5h.01"></path>
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 15.5h6"></path>
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M2 12h2m16 0h2"></path>
                        </svg>
                    </div>
                    <div>
                        <h4 class="font-bold text-sm tracking-wide">${chatbotTitle}</h4>
                        <span class="flex items-center gap-1.5 text-[11px] text-blue-100 font-medium">
                            <span class="w-2 h-2 rounded-full bg-secondary-green animate-pulse"></span>
                            ${chatbotStatus}
                        </span>
                    </div>
                </div>
                <button id="chatbot-minimize-btn" class="text-white/80 hover:text-white p-1 rounded-lg hover:bg-white/10 transition-colors">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                    </svg>
                </button>
            </div>

            <!-- Messages Area -->
            <div id="chatbot-messages" class="flex-1 p-4 overflow-y-auto space-y-3.5 bg-slate-50/50 text-sm">
                <!-- Welcome Message -->
                <div class="flex gap-2.5 items-start">
                    <div class="w-8 h-8 rounded-full bg-primary-blue text-white flex-shrink-0 flex items-center justify-center shadow-md shadow-blue-500/20">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 2a2 2 0 012 2v2h3.5a2.5 2.5 0 012.5 2.5v9a2.5 2.5 0 01-2.5 2.5h-11A2.5 2.5 0 014 17.5v-9A2.5 2.5 0 016.5 6H10V4a2 2 0 012-2z"></path>
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9.5 11.5h.01M14.5 11.5h.01"></path>
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 15.5h6"></path>
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M2 12h2m16 0h2"></path>
                        </svg>
                    </div>
                    <div class="bg-white p-3.5 rounded-2xl rounded-tl-none shadow-sm border border-slate-100 text-slate-700 max-w-[80%] leading-relaxed">
                        ${welcomeMsg}
                    </div>
                </div>
            </div>

            <!-- Typing Indicator (Hidden by default) -->
            <div id="chatbot-typing" class="hidden px-4 pb-2 bg-slate-50/50 flex items-center gap-2">
                <div class="w-6 h-6 rounded-full bg-primary-blue/10 text-primary-blue flex-shrink-0 flex items-center justify-center">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 2a2 2 0 012 2v2h3.5a2.5 2.5 0 012.5 2.5v9a2.5 2.5 0 01-2.5 2.5h-11A2.5 2.5 0 014 17.5v-9A2.5 2.5 0 016.5 6H10V4a2 2 0 012-2z"></path>
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9.5 11.5h.01M14.5 11.5h.01"></path>
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 15.5h6"></path>
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M2 12h2m16 0h2"></path>
                    </svg>
                </div>
                <div class="bg-slate-200/70 px-3 py-2 rounded-full flex items-center gap-1">
                    <div class="w-1.5 h-1.5 bg-slate-500 rounded-full animate-bounce"></div>
                    <div class="w-1.5 h-1.5 bg-slate-500 rounded-full animate-bounce [animation-delay:0.2s]"></div>
                    <div class="w-1.5 h-1.5 bg-slate-500 rounded-full animate-bounce [animation-delay:0.4s]"></div>
                </div>
            </div>

            <!-- Input Area -->
            <div class="p-3 bg-white border-t border-slate-100">
                <form id="chatbot-form" class="flex items-center gap-2">
                    <input type="text" id="chatbot-input" placeholder="${placeholderMsg}" 
                           class="flex-1 bg-slate-100/80 border border-slate-200 px-4 py-2.5 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary-blue/20 focus:border-primary-blue focus:bg-white transition-all placeholder:text-slate-400">
                    <button type="submit" id="chatbot-send-btn" class="w-10 h-10 rounded-xl bg-primary-blue hover:bg-blue-600 text-white flex items-center justify-center shadow-md shadow-blue-500/20 transition-all hover:scale-105 active:scale-95 disabled:opacity-50 disabled:pointer-events-none">
                        <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                            <path d="M3.478 2.405a.75.75 0 00-.926.94l2.432 7.905H13.5a.75.75 0 010 1.5H4.984l-2.432 7.905a.75.75 0 00.926.94 60.519 60.519 0 0018.445-8.986.75.75 0 000-1.218A60.517 60.517 0 003.478 2.405z"></path>
                        </svg>
                    </button>
                </form>
            </div>
        </div>
    `;
    document.body.appendChild(chatbotContainer);

    // 2. Elements
    const toggleBtn = document.getElementById('chatbot-toggle-btn');
    const minimizeBtn = document.getElementById('chatbot-minimize-btn');
    const panel = document.getElementById('chatbot-panel');
    const iconMsg = document.getElementById('chatbot-icon-msg');
    const iconClose = document.getElementById('chatbot-icon-close');
    const form = document.getElementById('chatbot-form');
    const input = document.getElementById('chatbot-input');
    const messagesBox = document.getElementById('chatbot-messages');
    const typingIndicator = document.getElementById('chatbot-typing');
    const sendBtn = document.getElementById('chatbot-send-btn');

    let isOpen = false;

    function toggleChat() {
        isOpen = !isOpen;
        if (isOpen) {
            panel.classList.remove('translate-y-4', 'opacity-0', 'pointer-events-none');
            panel.classList.add('translate-y-0', 'opacity-100', 'pointer-events-auto');
            iconMsg.classList.add('hidden');
            iconClose.classList.remove('hidden');
            input.focus();
        } else {
            panel.classList.add('translate-y-4', 'opacity-0', 'pointer-events-none');
            panel.classList.remove('translate-y-0', 'opacity-100', 'pointer-events-auto');
            iconMsg.classList.remove('hidden');
            iconClose.classList.add('hidden');
        }
    }

    toggleBtn.addEventListener('click', toggleChat);
    minimizeBtn.addEventListener('click', toggleChat);

    function appendMessage(sender, text) {
        const msgDiv = document.createElement('div');
        msgDiv.className = 'flex gap-2.5 items-start ' + (sender === 'user' ? 'flex-row-reverse' : '');

        if (sender === 'user') {
            msgDiv.innerHTML = `
                <div class="bg-gradient-to-r from-primary-blue to-blue-600 text-white p-3.5 rounded-2xl rounded-tr-none shadow-sm max-w-[80%] leading-relaxed break-words">
                    ${formatMessage(text)}
                </div>
            `;
        } else {
            msgDiv.innerHTML = `
                <div class="w-8 h-8 rounded-full bg-primary-blue text-white flex-shrink-0 flex items-center justify-center shadow-md shadow-blue-500/20">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 2a2 2 0 012 2v2h3.5a2.5 2.5 0 012.5 2.5v9a2.5 2.5 0 01-2.5 2.5h-11A2.5 2.5 0 014 17.5v-9A2.5 2.5 0 016.5 6H10V4a2 2 0 012-2z"></path>
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9.5 11.5h.01M14.5 11.5h.01"></path>
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 15.5h6"></path>
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M2 12h2m16 0h2"></path>
                    </svg>
                </div>
                <div class="bg-white p-3.5 rounded-2xl rounded-tl-none shadow-sm border border-slate-100 text-slate-700 max-w-[80%] leading-relaxed break-words whitespace-pre-line">
                    ${formatMessage(text)}
                </div>
            `;
        }
        messagesBox.appendChild(msgDiv);
        messagesBox.scrollTop = messagesBox.scrollHeight;
    }

    function escapeHtml(unsafe) {
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function formatMessage(text) {
        let safeText = escapeHtml(text);
        // Replace **text** with <b>text</b>
        safeText = safeText.replace(/\*\*(.*?)\*\*/g, '<b>$1</b>');
        return safeText;
    }

    form.addEventListener('submit', async function (e) {
        e.preventDefault();
        const text = input.value.trim();
        if (!text) return;

        appendMessage('user', text);
        input.value = '';
        sendBtn.disabled = true;
        typingIndicator.classList.remove('hidden');
        messagesBox.scrollTop = messagesBox.scrollHeight;

        try {
            const reqLang = document.documentElement.lang || 'vi';
            const res = await fetch('/api/public/chatbot', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: text, language: reqLang })
            });

            if (res.ok) {
                const data = await res.json();
                appendMessage('ai', data.reply || 'Xin lỗi, tôi không nhận được phản hồi.');
            } else {
                appendMessage('ai', 'Xin lỗi, hiện tại hệ thống AI đang bận. Vui lòng thử lại sau giây lát.');
            }
        } catch (err) {
            appendMessage('ai', 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra lại đường truyền.');
        } finally {
            typingIndicator.classList.add('hidden');
            sendBtn.disabled = false;
            input.focus();
        }
    });
});
