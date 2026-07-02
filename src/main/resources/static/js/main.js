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
});
