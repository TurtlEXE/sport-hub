// main.js - General UI interactions

document.addEventListener('DOMContentLoaded', () => {
    // 1. Mobile Menu Toggle
    const mobileMenuBtn = document.getElementById('mobile-menu-toggle-btn');
    const mobileMenuPanel = document.getElementById('mobile-menu-panel');
    
    if (mobileMenuBtn && mobileMenuPanel) {
        mobileMenuBtn.addEventListener('click', () => {
            mobileMenuPanel.classList.toggle('hidden');
        });
    }

    // 2. Smooth Scroll for Anchor Links (if any)
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            const targetId = this.getAttribute('href');
            if (targetId === '#') return;
            
            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                e.preventDefault();
                targetElement.scrollIntoView({
                    behavior: 'smooth'
                });
                
                // close mobile menu if open
                if (mobileMenuPanel && !mobileMenuPanel.classList.contains('hidden')) {
                    mobileMenuPanel.classList.add('hidden');
                }
            }
        });
    });

    // 3. Password Show/Hide Toggle
    const togglePasswordBtns = document.querySelectorAll('.toggle-password');
    togglePasswordBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const inputId = btn.getAttribute('data-target');
            const input = document.getElementById(inputId);
            if (input) {
                if (input.type === 'password') {
                    input.type = 'text';
                    btn.classList.remove('text-slate-400');
                    btn.classList.add('text-primary-blue');
                } else {
                    input.type = 'password';
                    btn.classList.remove('text-primary-blue');
                    btn.classList.add('text-slate-400');
                }
            }
        });
    });

    // 4. Initialize Universal Full-Page Translator (Google Translate DOM Engine)
    initUniversalTranslator();
});

// ============================================================================
// UNIVERSAL FULL-PAGE TRANSLATOR (Translates both static & dynamic database text)
// ============================================================================
window.googleTranslateElementInit = function() {
    new google.translate.TranslateElement({
        pageLanguage: 'auto',
        includedLanguages: 'en,vi,my,ja,ko,zh-CN,fr,de,ru,es,th',
        autoDisplay: false
    }, 'google_translate_element');
};

function initUniversalTranslator() {
    // 1. Sync googtrans cookie with active server lang parameter (if any)
    const urlParams = new URLSearchParams(window.location.search);
    const langParam = urlParams.get('lang');
    if (langParam && ['en', 'vi', 'my', 'ja', 'ko', 'zh-CN', 'fr', 'de', 'ru', 'es', 'th'].includes(langParam)) {
        const expectedCookie = "/auto/" + langParam;
        if (!document.cookie.includes("googtrans=" + expectedCookie)) {
            document.cookie = "googtrans=" + expectedCookie + "; path=/; max-age=" + (86400 * 365);
            if (window.location.hostname && window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1') {
                document.cookie = "googtrans=" + expectedCookie + "; path=/; domain=" + window.location.hostname + "; max-age=" + (86400 * 365);
            }
        }
    }

    // 2. Ensure hidden container exists for Google Translate DOM widget
    if (!document.getElementById('google_translate_element')) {
        const div = document.createElement('div');
        div.id = 'google_translate_element';
        div.style.display = 'none';
        document.body.appendChild(div);
    }

    // 3. Actively prevent Google Translate from shifting body (top: 40px) or displaying top banner frame
    const preventGoogleBannerShift = () => {
        if (document.body.style.top && document.body.style.top !== '0px') {
            document.body.style.setProperty('top', '0px', 'important');
        }
        if (document.body.style.position === 'relative') {
            document.body.style.setProperty('position', 'static', 'important');
        }
        document.querySelectorAll('iframe.skiptranslate, iframe.VIpgJd-ZVi9ni-ORHb-OEVmcd, iframe.goog-te-banner-frame, .VIpgJd-ZVi9ni-ORHb-OEVmcd, .skiptranslate > iframe').forEach(el => {
            el.style.setProperty('display', 'none', 'important');
            el.style.setProperty('visibility', 'hidden', 'important');
            el.style.setProperty('height', '0px', 'important');
        });
    };

    const observer = new MutationObserver(preventGoogleBannerShift);
    if (document.body) {
        observer.observe(document.body, { attributes: true, attributeFilter: ['style', 'class'], childList: true });
        setInterval(preventGoogleBannerShift, 200);
    }

    // 4. Inject Google Translate element script if not loaded
    if (!document.querySelector('script[src*="translate.google.com/translate_a/element.js"]')) {
        const script = document.createElement('script');
        script.src = 'https://translate.google.com/translate_a/element.js?cb=googleTranslateElementInit';
        script.async = true;
        document.head.appendChild(script);
    }
}

function changeLanguage(lang) {
    const targetLang = lang === 'vn' ? 'vi' : (lang === 'mm' ? 'my' : lang);
    
    // 1. Set Google Translate universal DOM translation cookie
    document.cookie = "googtrans=/auto/" + targetLang + "; path=/; max-age=" + (86400 * 365);
    if (window.location.hostname && window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1') {
        document.cookie = "googtrans=/auto/" + targetLang + "; path=/; domain=" + window.location.hostname + "; max-age=" + (86400 * 365);
    }
    
    // 2. Update server locale ?lang= parameter and reload page
    const url = new URL(window.location.href);
    url.searchParams.set('lang', targetLang);
    window.location.href = url.toString();
}
