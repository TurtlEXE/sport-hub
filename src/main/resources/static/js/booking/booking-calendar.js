/**
 * Booking Calendar Logic - Redesigned Two-Column Layout
 */
document.addEventListener('DOMContentLoaded', () => {
    // We get venueId injected from inline script in HTML
    let currentFacilitySportId = document.querySelector('.fs-tab') ? document.querySelector('.fs-tab').dataset.fsId : null;
    let currentDate = new Date().toISOString().split('T')[0];

    // Configs from API
    let config = {
        openingTime: "05:00",
        closingTime: "23:00",
        slotDurationMinutes: 30,
        minBookingDurationMinutes: 60
    };

    // State
    let selectedSlots = new Map(); // key: "courtId-slotIndex", value: slot object

    // Step 2 State
    let checkoutServices = [];
    let checkoutVouchers = [];
    let selectedServicesObj = {}; // { productId: qty }
    let baseTotal = 0; // Courts total

    // DOM Elements
    const timelineContainer = document.getElementById('timelineContainer');
    const timelineGrid = document.getElementById('timelineGrid');
    const loadingOverlay = document.getElementById('loadingOverlay');
    const currentDateDisplay = document.getElementById('currentDateDisplay');

    // Right Summary Elements
    const summarySlotsList = document.getElementById('summarySlotsList');
    const summaryTotalHours = document.getElementById('summaryTotalHours');
    const summaryBaseTotal = document.getElementById('summaryBaseTotal');
    const summaryServicesRow = document.getElementById('summaryServicesRow');
    const summaryServicesTotal = document.getElementById('summaryServicesTotal');
    const summaryDiscountRow = document.getElementById('summaryDiscountRow');
    const summaryDiscountTotal = document.getElementById('summaryDiscountTotal');
    const summaryFinalTotal = document.getElementById('summaryFinalTotal');

    const btnNextStep = document.getElementById('btnNextStep');
    const checkoutActionButtons = document.getElementById('checkoutActionButtons');

    // Step Containers
    const step1Booking = document.getElementById('step1-booking');
    const step2Checkout = document.getElementById('step2-checkout');

    // Initialize DatePicker
    let fpInstance = null;
    try {
        fpInstance = flatpickr("#datePickerInput", {
            locale: "vn",
            dateFormat: "Y-m-d",
            defaultDate: currentDate,
            minDate: "today",
            closeOnSelect: false,
            onChange: function(selectedDates, dateStr, instance) {
                // Do not update the global state yet, wait for Confirm button
            },
            onOpen: function() {
                let backdrop = document.getElementById('flatpickr-backdrop');
                if (!backdrop) {
                    backdrop = document.createElement('div');
                    backdrop.id = 'flatpickr-backdrop';
                    document.body.appendChild(backdrop);
                }
                backdrop.classList.add('show');
            },
            onClose: function() {
                const backdrop = document.getElementById('flatpickr-backdrop');
                if (backdrop) {
                    backdrop.classList.remove('show');
                }
            },
            onReady: function(selectedDates, dateStr, instance) {
                const footer = document.createElement('div');
                footer.className = "flex justify-end gap-3 p-3 mt-2 border-t border-slate-100";
                
                const btnCancel = document.createElement('button');
                btnCancel.className = "px-4 py-2 text-sm font-medium text-green-700 hover:bg-green-50 rounded-md transition-colors";
                btnCancel.textContent = "Hủy";
                btnCancel.onclick = function() {
                    instance.setDate(currentDate); // revert
                    instance.close();
                };
                
                const btnConfirm = document.createElement('button');
                btnConfirm.className = "px-5 py-2 text-sm font-medium text-white bg-green-700 hover:bg-green-800 rounded-md transition-colors shadow-sm";
                btnConfirm.textContent = "Xác nhận";
                btnConfirm.onclick = function() {
                    if (instance.selectedDates.length > 0) {
                        currentDate = instance.formatDate(instance.selectedDates[0], "Y-m-d");
                        updateDateDisplay(instance.selectedDates[0]);
                        loadTimelineData();
                    }
                    instance.close();
                };
                
                footer.appendChild(btnCancel);
                footer.appendChild(btnConfirm);
                instance.calendarContainer.appendChild(footer);
            }
        });
        
        window.openDatePicker = function() {
            if (fpInstance) {
                fpInstance.open();
            }
        };
    } catch (e) {
        console.warn("Flatpickr failed to initialize:", e);
    }

    // Initial display
    updateDateDisplay(new Date());
    if (currentFacilitySportId) {
        loadTimelineData();
    }

    // --- Global Function for Tab Switching ---
    window.switchTab = function (btn) {
        // Reset all tabs
        document.querySelectorAll('.fs-tab').forEach(el => {
            el.classList.remove('text-blue-600', 'border-blue-600');
            el.classList.add('text-slate-500', 'border-transparent');
        });

        // Highlight clicked tab
        btn.classList.remove('text-slate-500', 'border-transparent');
        btn.classList.add('text-blue-600', 'border-blue-600');

        // Update state and load
        currentFacilitySportId = btn.dataset.fsId;
        loadTimelineData();
    };

    window.openPricingModal = function () {
        document.querySelectorAll('.pricing-table').forEach(t => t.classList.add('hidden'));
        const activeTable = document.getElementById('pricing-table-' + currentFacilitySportId);
        if (activeTable) {
            activeTable.classList.remove('hidden');
        }
        document.getElementById('modalPricing').classList.remove('hidden');
    };

    /**
     * Load Timeline Data from API
     */
    async function loadTimelineData() {
        showLoading(true);
        selectedSlots.clear();
        validateAndUpdateRightSummary();

        if (!currentFacilitySportId) {
            timelineGrid.innerHTML = `<div class="p-8 text-center text-slate-500 col-span-full">Cơ sở này chưa cấu hình môn thể thao.</div>`;
            showLoading(false);
            return;
        }

        try {
            const res = await fetch(`/api/booking/timeline?facilitySportId=${currentFacilitySportId}&date=${currentDate}`);
            if (!res.ok) throw new Error("Failed to fetch data");

            const data = await res.json();

            if (data.isClosedToday) {
                timelineGrid.innerHTML = `<div class="p-8 text-center text-slate-500 font-medium col-span-full">Sân không hoạt động vào ngày này.</div>`;
                showLoading(false);
                return;
            }

            // Update config
            config.slotDurationMinutes = data.slotDurationMinutes || 30;
            config.minBookingDurationMinutes = data.minBookingDurationMinutes || 60;

            const minDurText = document.getElementById('minDurationText');
            if (minDurText) {
                minDurText.textContent = `Thời gian thuê tối thiểu: ${config.minBookingDurationMinutes} phút`;
                minDurText.classList.remove('hidden');
            }

            renderGrid(data);
        } catch (error) {
            console.error("Error loading timeline:", error);
            timelineGrid.innerHTML = `<div class="p-8 text-center text-slate-500 col-span-full">Lỗi khi tải dữ liệu. Vui lòng thử lại.</div>`;
        } finally {
            showLoading(false);
        }
    }

    function isSlotInPast(dateStr, startTimeStr) {
        const today = new Date();
        const todayStr = today.getFullYear() + "-" + String(today.getMonth() + 1).padStart(2, '0') + "-" + String(today.getDate()).padStart(2, '0');
        
        if (dateStr < todayStr) return true;
        if (dateStr > todayStr) return false;
        
        const [slotHour, slotMinute] = startTimeStr.split(':').map(Number);
        const currentHour = today.getHours();
        const currentMinute = today.getMinutes();
        
        if (slotHour < currentHour) return true;
        if (slotHour === currentHour && slotMinute <= currentMinute) return true; // considering it past if it has already started
        
        return false;
    }

    function renderGrid(data) {
        timelineGrid.innerHTML = '';
        const courts = data.courts || [];
        if (courts.length === 0) {
            timelineGrid.innerHTML = `<div class="p-8 text-center text-slate-500 col-span-full">Không có sân nào hoạt động.</div>`;
            return;
        }

        const numSlots = courts[0].slots.length;
        timelineGrid.style.setProperty('--total-slots', numSlots);

        // Render Header Row
        const headerFrag = document.createDocumentFragment();
        const cornerCell = document.createElement('div');
        cornerCell.className = 'timeline-header court-name text-xs uppercase tracking-wider text-slate-400 font-bold border-b border-slate-200';
        cornerCell.textContent = 'SÂN';
        headerFrag.appendChild(cornerCell);

        courts[0].slots.forEach(slot => {
            const timeCell = document.createElement('div');
            timeCell.className = 'timeline-header timeline-cell font-medium text-xs';
            timeCell.textContent = slot.startTime;
            headerFrag.appendChild(timeCell);
        });
        timelineGrid.appendChild(headerFrag);

        // Render Courts and Slots
        courts.forEach(court => {
            const courtFrag = document.createDocumentFragment();

            const nameCell = document.createElement('div');
            nameCell.className = 'timeline-cell court-name text-xs text-slate-700';
            nameCell.textContent = court.courtName;
            courtFrag.appendChild(nameCell);

            court.slots.forEach(slot => {
                const slotCell = document.createElement('div');

                slotCell.className = 'timeline-cell transition-all relative';
                slotCell.dataset.courtId = court.courtId;
                slotCell.dataset.courtName = court.courtName;
                slotCell.dataset.slotIndex = slot.slotIndex;
                slotCell.dataset.startTime = slot.startTime;
                slotCell.dataset.endTime = slot.endTime;
                slotCell.dataset.price = slot.price || 0;

                const isPast = isSlotInPast(currentDate, slot.startTime);

                if (slot.status === 'BOOKED') {
                    slotCell.classList.add('slot-booked');
                    slotCell.title = "Đã có người đặt";
                } else if (slot.status === 'LOCKED') {
                    slotCell.classList.add('slot-locked');
                    slotCell.title = "Sân nghỉ / Khoá";
                } else if (isPast) {
                    slotCell.classList.add('slot-locked');
                    slotCell.title = "Đã qua giờ";
                } else {
                    slotCell.classList.add('slot-available');
                    slotCell.title = `${slot.price ? formatMoney(slot.price) + 'đ' : 'Trống'}`;
                    slotCell.addEventListener('click', () => handleSlotClick(slotCell, court, slot));
                }

                courtFrag.appendChild(slotCell);
            });
            timelineGrid.appendChild(courtFrag);
        });
    }

    function handleSlotClick(cell, court, slot) {
        const key = `${court.courtId}-${slot.slotIndex}`;

        if (selectedSlots.has(key)) {
            selectedSlots.delete(key);
            cell.classList.remove('slot-selected');
        } else {
            selectedSlots.set(key, {
                courtId: court.courtId,
                courtName: court.courtName,
                slotIndex: slot.slotIndex,
                startTime: slot.startTime,
                endTime: slot.endTime,
                price: slot.price || 0
            });
            cell.classList.add('slot-selected');
        }

        validateAndUpdateRightSummary();
    }

    function validateAndUpdateRightSummary() {
        if (selectedSlots.size === 0) {
            summarySlotsList.innerHTML = '<div class="text-slate-500 text-center py-6 bg-white rounded-lg border border-slate-200 border-dashed">Vui lòng chọn ca trên lịch</div>';
            summaryTotalHours.textContent = "0 giờ";
            baseTotal = 0;
            btnNextStep.disabled = true;
            updateFinalTotal();
            return;
        }

        const courtGroups = new Map();
        selectedSlots.forEach(slot => {
            if (!courtGroups.has(slot.courtId)) courtGroups.set(slot.courtId, []);
            courtGroups.get(slot.courtId).push(slot);
        });

        let allValid = true;
        let continuousBlocks = [];
        let totalMins = 0;
        baseTotal = 0;

        courtGroups.forEach((slots, courtId) => {
            slots.sort((a, b) => a.slotIndex - b.slotIndex);
            let currentBlock = [slots[0]];
            for (let i = 1; i < slots.length; i++) {
                if (slots[i].slotIndex === slots[i - 1].slotIndex + 1) {
                    currentBlock.push(slots[i]);
                } else {
                    continuousBlocks.push(currentBlock);
                    currentBlock = [slots[i]];
                }
            }
            continuousBlocks.push(currentBlock);
        });

        continuousBlocks.forEach(block => {
            const blockDuration = block.length * config.slotDurationMinutes;
            if (blockDuration < config.minBookingDurationMinutes) {
                allValid = false;
            }
            totalMins += blockDuration;
            block.forEach(s => baseTotal += s.price);
        });

        // Update UI
        renderSummaryBlocksUI(continuousBlocks, allValid);
        summaryTotalHours.textContent = formatDuration(totalMins);
        updateFinalTotal();

        if (!allValid) {
            btnNextStep.disabled = true;
        } else {
            btnNextStep.disabled = false;
        }
    }

    function renderSummaryBlocksUI(blocks, allValid) {
        summarySlotsList.innerHTML = '';
        blocks.forEach(block => {
            const courtName = block[0].courtName;
            const startTime = block[0].startTime;
            const endTime = block[block.length - 1].endTime;
            const blockDuration = block.length * config.slotDurationMinutes;
            const blockPrice = block.reduce((sum, slot) => sum + slot.price, 0);
            const isValid = blockDuration >= config.minBookingDurationMinutes;

            const item = document.createElement('div');
            item.className = 'bg-white p-3.5 rounded-lg border border-slate-200 flex flex-col gap-1.5 shadow-sm';

            let html = `
                <div class="flex justify-between items-start">
                    <div>
                        <div class="font-bold text-slate-800">Sân ${courtName}</div>
                        <div class="text-blue-600 font-medium text-xs mt-1 tracking-wide">${startTime} - ${endTime}</div>
                    </div>
                    <div class="text-right">
                        <div class="font-semibold text-slate-800">${formatMoney(blockPrice)} đ</div>
                        <div class="text-xs text-slate-500 mt-1">${formatDuration(blockDuration)}</div>
                    </div>
                </div>
            `;
            if (!isValid) {
                html += `<div class="text-xs text-red-500 mt-1 flex items-center gap-1"><svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>Thiếu ${config.minBookingDurationMinutes - blockDuration}p tối thiểu</div>`;
                item.classList.add('border-red-300', 'bg-red-50');
            }

            item.innerHTML = html;
            summarySlotsList.appendChild(item);
        });
    }

    function updateDateDisplay(dateObj) {
        const d = dateObj.getDate().toString().padStart(2, '0');
        const m = (dateObj.getMonth() + 1).toString().padStart(2, '0');
        const y = dateObj.getFullYear();
        currentDateDisplay.textContent = `${d}/${m}/${y}`;
    }

    function showLoading(show) {
        loadingOverlay.classList.toggle('hidden', !show);
    }

    function formatMoney(amount) {
        return amount.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
    }

    function formatDuration(totalMins) {
        const h = Math.floor(totalMins / 60);
        const m = totalMins % 60;
        if (h > 0 && m > 0) return `${h}h${m}`;
        if (h > 0) return `${h} giờ`;
        return `${m} phút`;
    }

    // --- CHECKOUT LOGIC & STEP TOGGLING ---

    btnNextStep.addEventListener('click', () => {
        if (btnNextStep.disabled) return;

        // Hide Step 1, Show Step 2
        step1Booking.classList.add('hidden');
        step2Checkout.classList.remove('hidden');
        step2Checkout.classList.add('flex');

        // Swap Buttons
        btnNextStep.classList.add('hidden');
        checkoutActionButtons.classList.remove('hidden');
        checkoutActionButtons.classList.add('flex');

        // Fetch dependencies for step 2 if not yet
        if (checkoutServices.length === 0) fetchServices();
        if (checkoutVouchers.length === 0) fetchVouchers();
    });

    window.backToStep1 = function () {
        step2Checkout.classList.add('hidden');
        step2Checkout.classList.remove('flex');
        step1Booking.classList.remove('hidden');

        checkoutActionButtons.classList.add('hidden');
        checkoutActionButtons.classList.remove('flex');
        btnNextStep.classList.remove('hidden');
    };

    async function fetchServices() {
        try {
            const res = await fetch(`/api/booking/services?facilityId=${venueId}`);
            if (res.ok) {
                checkoutServices = await res.json();
            }
        } catch (e) { console.error("Err fetching services", e); }
    }

    async function fetchVouchers() {
        const platformSelect = document.getElementById('platformVoucher');
        const facilitySelect = document.getElementById('facilityVoucher');
        if (!platformSelect) return;

        try {
            const res = await fetch(`/api/booking/vouchers?facilityId=${venueId}`);
            if (res.ok) {
                checkoutVouchers = await res.json();
                platformSelect.innerHTML = '<option value="">-- Chọn ưu đãi --</option>';
                facilitySelect.innerHTML = '<option value="">-- Chọn ưu đãi --</option>';

                checkoutVouchers.forEach(v => {
                    const opt = `<option value="${v.voucherId}">${v.name} - Giảm ${formatMoney(v.discountValue)}${v.discountType === 'PERCENTAGE' ? '%' : 'đ'}</option>`;
                    if (v.issuerType === 'PLATFORM') {
                        platformSelect.innerHTML += opt;
                    } else {
                        facilitySelect.innerHTML += opt;
                    }
                });
            }
        } catch (e) { console.error("Err fetching vouchers", e); }
    }

    window.openServicesModal = function () {
        const container = document.getElementById('servicesListContainer');
        if (checkoutServices.length === 0) {
            container.innerHTML = '<div class="text-center text-slate-500 py-8 text-sm">Chưa có dịch vụ nào cho cơ sở này.</div>';
        } else {
            let html = '';
            checkoutServices.forEach(srv => {
                const qty = selectedServicesObj[srv.productId] || 0;
                const imgHtml = srv.imagePath 
                    ? `<img src="${srv.imagePath}" class="w-14 h-14 rounded-lg object-cover border border-slate-100" />`
                    : `<div class="w-14 h-14 rounded-lg bg-slate-100 flex items-center justify-center text-slate-400 border border-slate-200"><svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg></div>`;
                
                html += `
                    <div class="flex items-center justify-between p-4 bg-white border border-slate-200 rounded-xl hover:border-blue-300 transition-colors">
                        <div class="flex items-center gap-4">
                            ${imgHtml}
                            <div>
                                <div class="font-bold text-slate-800">${srv.productName}</div>
                                <div class="text-sm text-slate-500 mt-0.5">${formatMoney(srv.price)} đ / ${srv.unit || 'Lượt'}</div>
                            </div>
                        </div>
                        <div class="flex items-center bg-slate-50 border border-slate-200 rounded-lg p-1 shrink-0">
                            <button onclick="updateServiceQty(${srv.productId}, -1)" class="w-8 h-8 rounded-md flex items-center justify-center text-slate-600 hover:bg-white hover:shadow-sm transition-all">-</button>
                            <span id="qty-${srv.productId}" class="w-10 text-center font-bold text-slate-800">${qty}</span>
                            <button onclick="updateServiceQty(${srv.productId}, 1)" class="w-8 h-8 rounded-md bg-blue-600 flex items-center justify-center text-white hover:bg-blue-700 shadow-sm transition-all">+</button>
                        </div>
                    </div>
                `;
            });
            container.innerHTML = html;
        }
        updateServicesModalSubtotal();
        document.getElementById('modalServices').classList.remove('hidden');
    };

    window.updateServiceQty = function (productId, delta) {
        const srv = checkoutServices.find(s => s.productId === productId);
        if (!srv) return;

        let current = selectedServicesObj[productId] || 0;
        current += delta;
        if (current < 0) current = 0;
        if (srv.stock && current > srv.stock) {
            alert('Vượt quá số lượng tồn kho!');
            return;
        }

        selectedServicesObj[productId] = current;
        document.getElementById('qty-' + productId).innerText = current;

        renderSelectedServicesList();
        updateServicesModalSubtotal();
        window.calculateCheckoutTotal(); // Update right sticky
    };

    function updateServicesModalSubtotal() {
        let st = 0;
        for (const [pId, qty] of Object.entries(selectedServicesObj)) {
            const srv = checkoutServices.find(s => s.productId == pId);
            if (srv) st += (srv.price * qty);
        }
        document.getElementById('modalServicesSubtotal').innerText = formatMoney(st) + ' đ';
    }

    function renderSelectedServicesList() {
        const container = document.getElementById('checkoutSelectedServices');
        let html = '';
        for (const [pId, qty] of Object.entries(selectedServicesObj)) {
            if (qty > 0) {
                const srv = checkoutServices.find(s => s.productId == pId);
                if (srv) {
                    html += `
                    <div class="flex justify-between items-center text-sm py-2 border-b border-slate-100 last:border-0">
                        <span class="text-slate-700">${srv.productName} <span class="text-slate-400">× ${qty}</span></span>
                        <span class="font-medium text-slate-800">${formatMoney(srv.price * qty)} đ</span>
                    </div>`;
                }
            }
        }
        container.innerHTML = html;
    }

    window.calculateCheckoutTotal = function () {
        updateFinalTotal();
    };

    function updateFinalTotal() {
        let servicesTotal = 0;
        for (const [pId, qty] of Object.entries(selectedServicesObj)) {
            const srv = checkoutServices.find(s => s.productId == pId);
            if (srv) servicesTotal += (srv.price * qty);
        }

        if (servicesTotal > 0) {
            summaryServicesRow.classList.remove('hidden');
            summaryServicesTotal.textContent = formatMoney(servicesTotal) + ' đ';
        } else {
            summaryServicesRow.classList.add('hidden');
        }

        let subtotal = baseTotal + servicesTotal;
        let totalDiscount = 0;

        const platformSelect = document.getElementById('platformVoucher');
        const facilitySelect = document.getElementById('facilityVoucher');

        const applyVoucher = (vId) => {
            const v = checkoutVouchers.find(x => x.voucherId == vId);
            if (v && subtotal >= (v.minOrderAmount || 0)) {
                let discount = 0;
                if (v.discountType === 'PERCENTAGE') {
                    discount = subtotal * (v.discountValue / 100);
                    if (v.maxDiscountAmount && discount > v.maxDiscountAmount) {
                        discount = v.maxDiscountAmount;
                    }
                } else {
                    discount = v.discountValue;
                }
                totalDiscount += discount;
            }
        };

        if (platformSelect && platformSelect.value) applyVoucher(platformSelect.value);
        if (facilitySelect && facilitySelect.value) applyVoucher(facilitySelect.value);

        if (totalDiscount > 0) {
            summaryDiscountRow.classList.remove('hidden');
            summaryDiscountRow.classList.add('flex');
            summaryDiscountTotal.textContent = '-' + formatMoney(totalDiscount) + ' đ';
        } else {
            summaryDiscountRow.classList.add('hidden');
            summaryDiscountRow.classList.remove('flex');
        }

        let finalTotal = subtotal - totalDiscount;
        if (finalTotal < 0) finalTotal = 0;

        summaryFinalTotal.textContent = formatMoney(finalTotal) + ' đ';
    }

    document.getElementById('btnConfirmPayment').addEventListener('click', () => {
        alert("Tính năng gửi yêu cầu đặt sân đang được hoàn thiện. Tổng tiền: " + summaryFinalTotal.textContent);
    });
});
