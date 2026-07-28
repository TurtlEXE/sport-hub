let currentFacilitySportId = null;
let currentFacilityId = null;
let currentDateObj = new Date();
let currentDate = currentDateObj.toISOString().split('T')[0];

let timelineGrid = null;
let loadingOverlay = null;
let currentDateDisplay = null;
let facilityTabsContainer = null;
let sportTabsContainer = null;

let facilitiesMap = {};
let facilitiesList = [];

let selectedSlots = []; // { courtId, courtName, startTime, endTime, price }
let facilityProducts = [];
let selectedProductsMap = {}; // productId -> qty
let fpInstance = null;

document.addEventListener('DOMContentLoaded', function () {
    timelineGrid = document.getElementById('timelineGrid');
    loadingOverlay = document.getElementById('loadingOverlay');
    currentDateDisplay = document.getElementById('currentDateDisplay');
    facilityTabsContainer = document.getElementById('facilityTabsContainer');
    sportTabsContainer = document.getElementById('sportTabsContainer');

    parseFacilitiesData();
    initDatePicker();
    updateDateDisplay(currentDateObj);
    initTabs();
});

function parseFacilitiesData() {
    const fsDataElements = document.querySelectorAll('.fs-item');
    facilitiesMap = {};
    fsDataElements.forEach(el => {
        const fsId = el.dataset.fsId;
        const fId = el.dataset.facilityId;
        const fName = el.dataset.facilityName;
        const sId = el.dataset.sportId;
        const sName = el.dataset.sportName;

        if (!facilitiesMap[fId]) {
            facilitiesMap[fId] = { id: fId, name: fName, sports: [] };
        }
        facilitiesMap[fId].sports.push({ fsId: fsId, id: sId, name: sName });
    });
    facilitiesList = Object.values(facilitiesMap);
}

function initTabs() {
    if (!facilityTabsContainer) return;
    facilityTabsContainer.innerHTML = '';

    if (facilitiesList.length === 0) {
        facilityTabsContainer.innerHTML = '<div class="px-6 py-4 text-sm text-slate-500 italic">No active facilities or sports found.</div>';
        return;
    }

    // Render Facility Tabs
    facilitiesList.forEach((fac, index) => {
        const btn = document.createElement('button');
        btn.className = 'folder-tab fac-tab';
        if (index === 0) {
            btn.classList.add('folder-tab-active');
            currentFacilityId = fac.id;
        } else {
            btn.classList.add('folder-tab-inactive');
        }
        btn.textContent = fac.name;
        btn.onclick = () => switchFacilityTab(fac.id, btn);
        facilityTabsContainer.appendChild(btn);
    });

    if (currentFacilityId) {
        renderSportTabs(currentFacilityId);
    }
}

function switchFacilityTab(facilityId, btn) {
    document.querySelectorAll('.fac-tab').forEach(el => {
        el.classList.remove('folder-tab-active');
        el.classList.add('folder-tab-inactive');
    });

    btn.classList.remove('folder-tab-inactive');
    btn.classList.add('folder-tab-active');

    currentFacilityId = facilityId;
    renderSportTabs(facilityId);
}

function renderSportTabs(facilityId) {
    if (!sportTabsContainer) return;
    sportTabsContainer.innerHTML = '';
    const fac = facilitiesMap[facilityId];
    if (!fac || fac.sports.length === 0) return;

    fac.sports.forEach((sport, index) => {
        const btn = document.createElement('button');
        btn.className = 'folder-tab sport-tab';
        btn.style.padding = '8px 20px';

        if (index === 0) {
            btn.classList.add('folder-tab-active');
            currentFacilitySportId = sport.fsId;
        } else {
            btn.classList.add('folder-tab-inactive');
        }
        btn.textContent = sport.name;
        btn.onclick = () => switchSportTab(sport.fsId, btn);
        sportTabsContainer.appendChild(btn);
    });

    if (currentFacilitySportId) {
        loadTimelineData();
    }
}

function switchSportTab(fsId, btn) {
    document.querySelectorAll('.sport-tab').forEach(el => {
        el.classList.remove('folder-tab-active');
        el.classList.add('folder-tab-inactive');
    });
    btn.classList.remove('folder-tab-inactive');
    btn.classList.add('folder-tab-active');

    currentFacilitySportId = fsId;
    loadTimelineData();
}

function initDatePicker() {
    try {
        fpInstance = flatpickr("#datePickerInput", {
            locale: "vn",
            dateFormat: "Y-m-d",
            defaultDate: currentDate,
            onChange: function (selectedDates, dateStr, instance) {
                if (selectedDates.length > 0) {
                    currentDateObj = selectedDates[0];
                    currentDate = dateStr;
                    updateDateDisplay(currentDateObj);
                    loadTimelineData();
                }
            }
        });

        window.openDatePicker = function () {
            if (fpInstance) {
                fpInstance.open();
            }
        };
    } catch (e) {
        console.warn("Flatpickr failed to initialize:", e);
    }
}

function updateDateDisplay(dateObj) {
    if (!currentDateDisplay) return;
    const d = dateObj.getDate().toString().padStart(2, '0');
    const m = (dateObj.getMonth() + 1).toString().padStart(2, '0');
    const y = dateObj.getFullYear();
    currentDateDisplay.textContent = `${d}/${m}/${y}`;
}

window.changeDate = function (days) {
    currentDateObj.setDate(currentDateObj.getDate() + days);
    currentDate = currentDateObj.toISOString().split('T')[0];
    updateDateDisplay(currentDateObj);
    if (fpInstance) {
        fpInstance.setDate(currentDateObj);
    }
    loadTimelineData();
};

async function loadTimelineData() {
    if (!currentFacilitySportId || !timelineGrid) return;

    if (loadingOverlay) loadingOverlay.classList.remove('hidden');

    try {
        const res = await fetch(`/api/owner/booking/timeline?facilitySportId=${currentFacilitySportId}&date=${currentDate}`);
        if (!res.ok) throw new Error("Failed to fetch data");

        const data = await res.json();
        currentMinDurationMinutes = data.minBookingDurationMinutes || data.minDurationMinutes || 60;
        renderGrid(data);
    } catch (error) {
        console.error("Error loading timeline:", error);
        timelineGrid.innerHTML = `<div class="p-8 text-center text-slate-500 col-span-full">Lỗi khi tải dữ liệu. Vui lòng thử lại.</div>`;
    } finally {
        if (loadingOverlay) loadingOverlay.classList.add('hidden');
    }
}

function renderGrid(data) {
    if (!timelineGrid) return;
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
    cornerCell.textContent = 'COURT / TIME';
    headerFrag.appendChild(cornerCell);

    courts[0].slots.forEach(slot => {
        const timeCell = document.createElement('div');
        timeCell.className = 'timeline-header timeline-cell px-1 font-mono text-[10px] select-none';
        timeCell.innerHTML = `<div class="flex justify-between items-center w-full"><span class="font-bold text-slate-700">${slot.startTime}</span><span class="text-slate-400 font-normal">${slot.endTime}</span></div>`;
        headerFrag.appendChild(timeCell);
    });
    timelineGrid.appendChild(headerFrag);

    // Render Courts and Slots
    courts.forEach((court, courtIdx) => {
        const courtFrag = document.createDocumentFragment();

        const nameCell = document.createElement('div');
        nameCell.className = 'timeline-cell court-name text-xs text-slate-700';
        nameCell.textContent = court.courtName;
        courtFrag.appendChild(nameCell);

        court.slots.forEach(slot => {
            const slotCell = document.createElement('div');
            slotCell.className = 'timeline-cell';

            if (slot.status === 'BOOKED' || slot.status === 'HOLD') {
                const isBooked = slot.status === 'BOOKED';
                slotCell.classList.add(isBooked ? 'slot-booked' : 'slot-hold', 'cursor-pointer');
                if (courtIdx === 0) {
                    slotCell.classList.add('tooltip-down');
                }
                const bName = slot.bookerName || 'Guest';
                const bPhone = slot.bookerPhone || 'N/A';
                slotCell.innerHTML = `
                    <div class="px-1 text-center w-full overflow-hidden select-none pointer-events-none">
                        <div class="font-bold text-[11px] leading-tight truncate ${isBooked ? 'text-slate-100' : 'text-amber-950'}">${bName}</div>
                        <div class="text-[9px] ${isBooked ? 'text-slate-300' : 'text-amber-800'} font-mono tracking-tight truncate">${bPhone}</div>
                    </div>
                    <div class="booking-tooltip">
                        <div class="font-bold text-slate-800 mb-1 text-sm">${bName}</div>
                        <div class="text-slate-500 flex items-center gap-1.5 font-medium">
                            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/></svg>
                            ${bPhone}
                        </div>
                        <div class="text-blue-600 font-bold mt-1 text-[10px]">👉 Click for details</div>
                    </div>
                `;
                if (slot.bookingId) {
                    slotCell.onclick = (e) => {
                        e.stopPropagation();
                        fetchAndShowBookingDetail(slot.bookingId);
                    };
                }
            } else if (slot.status === 'PAST') {
                slotCell.classList.add('slot-past');
                slotCell.innerHTML = `<span class="text-[10px] text-slate-400 font-medium">Past</span>`;
            } else if (slot.status === 'UNPRICED') {
                slotCell.classList.add('slot-unpriced');
                slotCell.innerHTML = `<span class="text-[10px] text-rose-600 font-bold">Unpriced</span>`;
            } else {
                slotCell.classList.add('slot-available', 'cursor-pointer');
                const isSel = selectedSlots.some(s => s.courtId === court.courtId && s.startTime === slot.startTime);
                if (isSel) {
                    slotCell.classList.add('slot-selected');
                    slotCell.innerHTML = `<span class="font-bold text-xs text-white">Selected</span>`;
                }
                slotCell.onclick = () => toggleSlotSelect(court.courtId, court.courtName, slot, slotCell);
            }

            courtFrag.appendChild(slotCell);
        });
        timelineGrid.appendChild(courtFrag);
    });
}

// --- ON-SITE BOOKING LOGIC ---
function toggleSlotSelect(courtId, courtName, slot, cellElement) {
    const index = selectedSlots.findIndex(s => s.courtId === courtId && s.startTime === slot.startTime);
    if (index >= 0) {
        selectedSlots.splice(index, 1);
        cellElement.classList.remove('slot-selected');
        cellElement.innerHTML = '';
    } else {
        selectedSlots.push({
            courtId: courtId,
            courtName: courtName,
            startTime: slot.startTime,
            endTime: slot.endTime,
            price: slot.price || 0
        });
        cellElement.classList.add('slot-selected');
        cellElement.innerHTML = `<span class="font-bold text-xs text-white drop-shadow-xs">Selected</span>`;
    }
    updateSelectionSummaryBar();
}

window.clearSlotSelection = function () {
    selectedSlots = [];
    document.querySelectorAll('.slot-selected').forEach(cell => {
        cell.classList.remove('slot-selected');
        cell.innerHTML = '';
    });
    updateSelectionSummaryBar();
};

function parseTimeToMinutes(timeStr) {
    if (!timeStr) return 0;
    if (timeStr === '23:59') return 1440;
    const parts = timeStr.split(':');
    return parseInt(parts[0], 10) * 60 + parseInt(parts[1], 10);
}

function getContinuousBlocksPerCourt() {
    const courtMap = {};
    selectedSlots.forEach(s => {
        if (!courtMap[s.courtId]) courtMap[s.courtId] = [];
        courtMap[s.courtId].push(s);
    });

    const allBlocks = [];
    for (const courtId in courtMap) {
        const slots = courtMap[courtId];
        slots.sort((a, b) => parseTimeToMinutes(a.startTime) - parseTimeToMinutes(b.startTime));
        if (slots.length === 0) continue;

        let currentBlock = [slots[0]];
        for (let i = 1; i < slots.length; i++) {
            const prevEnd = parseTimeToMinutes(slots[i - 1].endTime);
            const currStart = parseTimeToMinutes(slots[i].startTime);
            if (currStart === prevEnd) {
                currentBlock.push(slots[i]);
            } else {
                allBlocks.push({ courtId: courtId, courtName: slots[0].courtName, slots: currentBlock });
                currentBlock = [slots[i]];
            }
        }
        allBlocks.push({ courtId: courtId, courtName: slots[0].courtName, slots: currentBlock });
    }
    return allBlocks;
}

function getBlockDurationMinutes(block) {
    if (!block || !block.slots || block.slots.length === 0) return 0;
    const firstStart = parseTimeToMinutes(block.slots[0].startTime);
    const lastEnd = parseTimeToMinutes(block.slots[block.slots.length - 1].endTime);
    return lastEnd > firstStart ? (lastEnd - firstStart) : (1440 - firstStart);
}

function validateMinDuration() {
    if (selectedSlots.length === 0) return false;

    const blocks = getContinuousBlocksPerCourt();
    for (const b of blocks) {
        const duration = getBlockDurationMinutes(b);
        if (currentMinDurationMinutes > 0 && duration < currentMinDurationMinutes) {
            const firstStart = b.slots[0].startTime;
            const lastEnd = b.slots[b.slots.length - 1].endTime;
            alert(`⚠️ Khung giờ liền nhau (${firstStart} - ${lastEnd}) trên ${b.courtName} mới chỉ có ${duration} phút.\nThời lượng đặt tối thiểu cho mỗi khung giờ liên tục là ${currentMinDurationMinutes} phút! Vui lòng chọn thêm ca liền kề.`);
            return false;
        }
    }
    return true;
}

function updateSelectionSummaryBar() {
    const bar = document.getElementById('selectionSummaryBar');
    if (!bar) return;

    if (selectedSlots.length === 0) {
        bar.classList.add('hidden');
        return;
    }

    bar.classList.remove('hidden');
    document.getElementById('selectedSlotsCount').textContent = selectedSlots.length;

    const totalCourtFee = selectedSlots.reduce((sum, s) => sum + Number(s.price), 0);
    document.getElementById('selectedSlotsCourtText').textContent = `Đã chọn ${selectedSlots.length} ca`;

    const blocks = getContinuousBlocksPerCourt();
    let hasUnmetMin = false;
    for (const b of blocks) {
        const duration = getBlockDurationMinutes(b);
        if (currentMinDurationMinutes > 0 && duration < currentMinDurationMinutes) {
            hasUnmetMin = true;
            break;
        }
    }

    const priceTextEl = document.getElementById('selectedSlotsPriceText');
    if (priceTextEl) {
        if (hasUnmetMin) {
            priceTextEl.innerHTML = `<span class="text-amber-300 font-bold">⚠️ Chưa đủ tối thiểu ${currentMinDurationMinutes} phút / khung giờ kề nhau</span> | Tổng tiền: ${formatVND(totalCourtFee)}`;
        } else {
            priceTextEl.textContent = `Tổng tiền: ${formatVND(totalCourtFee)}`;
        }
    }
}

function formatVND(amount) {
    return new Intl.NumberFormat('vi-VN').format(amount) + ' VNĐ';
}

window.openOnSiteBookingModal = async function () {
    if (selectedSlots.length === 0) {
        alert("Vui lòng click chọn ít nhất 1 ca trống trên lịch để đặt sân!");
        return;
    }

    if (!validateMinDuration()) {
        return;
    }

    const modal = document.getElementById('onSiteBookingModal');
    if (modal) modal.classList.remove('hidden');

    const listEl = document.getElementById('modalSelectedSlotsList');
    let courtFeeTotal = 0;
    if (listEl) {
        listEl.innerHTML = selectedSlots.map(s => {
            courtFeeTotal += Number(s.price);
            return `<div class="flex justify-between items-center py-1 border-b border-blue-100/50 last:border-none">
                <span>• ${s.courtName} (${s.startTime} - ${s.endTime})</span>
                <span class="font-bold text-blue-900">${formatVND(s.price)}</span>
            </div>`;
        }).join('');
    }

    const feeDisp = document.getElementById('modalCourtFeeDisplay');
    if (feeDisp) feeDisp.textContent = formatVND(courtFeeTotal);

    loadFacilityProducts();
    updateModalTotalFee();
};

window.closeOnSiteBookingModal = function () {
    const modal = document.getElementById('onSiteBookingModal');
    if (modal) modal.classList.add('hidden');
};

async function loadFacilityProducts() {
    const container = document.getElementById('modalProductsContainer');
    if (!container || !currentFacilityId) return;

    try {
        const res = await fetch(`/api/owner/booking/products?facilityId=${currentFacilityId}`);
        if (!res.ok) throw new Error("Failed to fetch products");
        facilityProducts = await res.json();

        if (facilityProducts.length === 0) {
            container.innerHTML = `<div class="text-xs text-slate-400 italic p-3 text-center col-span-full">Không có dịch vụ thêm nào tại cơ sở này.</div>`;
            return;
        }

        container.innerHTML = facilityProducts.map(p => {
            const qty = selectedProductsMap[p.productId] || 0;
            return `<div class="p-3 bg-white rounded-xl border border-slate-200 flex items-center justify-between shadow-xs">
                <div>
                    <div class="font-bold text-xs text-slate-800">${p.productName}</div>
                    <div class="text-[11px] text-emerald-600 font-semibold">${formatVND(p.price)} ${p.unit ? '/ ' + p.unit : ''}</div>
                </div>
                <div class="flex items-center gap-2">
                    <button onclick="changeProductQty(${p.productId}, -1)" class="w-6 h-6 rounded-md bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs flex items-center justify-center transition-colors">-</button>
                    <span id="prodQty-${p.productId}" class="text-xs font-bold text-slate-800 w-4 text-center">${qty}</span>
                    <button onclick="changeProductQty(${p.productId}, 1)" class="w-6 h-6 rounded-md bg-blue-100 hover:bg-blue-200 text-blue-700 font-bold text-xs flex items-center justify-center transition-colors">+</button>
                </div>
            </div>`;
        }).join('');
    } catch (e) {
        console.error(e);
        container.innerHTML = `<div class="text-xs text-slate-400 italic p-3 text-center col-span-full">Không thể tải dịch vụ.</div>`;
    }
}

window.changeProductQty = function (productId, delta) {
    const currentQty = selectedProductsMap[productId] || 0;
    const newQty = Math.max(0, currentQty + delta);
    selectedProductsMap[productId] = newQty;

    const qtyEl = document.getElementById(`prodQty-${productId}`);
    if (qtyEl) qtyEl.textContent = newQty;

    updateModalTotalFee();
};

function updateModalTotalFee() {
    const courtFee = selectedSlots.reduce((sum, s) => sum + Number(s.price), 0);
    let productFee = 0;
    facilityProducts.forEach(p => {
        const qty = selectedProductsMap[p.productId] || 0;
        productFee += (Number(p.price) * qty);
    });

    const grandTotal = courtFee + productFee;
    const totalDisp = document.getElementById('modalTotalFeeDisplay');
    if (totalDisp) totalDisp.textContent = formatVND(grandTotal);
}

window.updatePaymentMethodStyle = function () {
    const isCash = document.querySelector('input[name="modalPaymentMethod"]:checked').value === 'CASH';
    const labelCash = document.getElementById('labelPaymentCash');
    const labelVnpay = document.getElementById('labelPaymentVnpay');

    if (isCash) {
        if (labelCash) labelCash.className = "flex items-center gap-3 p-3 border rounded-xl cursor-pointer bg-slate-50 hover:bg-white transition-all border-emerald-500 ring-2 ring-emerald-500/20";
        if (labelVnpay) labelVnpay.className = "flex items-center gap-3 p-3 border border-slate-200 rounded-xl cursor-pointer bg-slate-50 hover:bg-white transition-all";
    } else {
        if (labelCash) labelCash.className = "flex items-center gap-3 p-3 border border-slate-200 rounded-xl cursor-pointer bg-slate-50 hover:bg-white transition-all";
        if (labelVnpay) labelVnpay.className = "flex items-center gap-3 p-3 border rounded-xl cursor-pointer bg-slate-50 hover:bg-white transition-all border-blue-500 ring-2 ring-blue-500/20";
    }
};

window.submitOnSiteBooking = async function () {
    const customerName = document.getElementById('modalCustomerName').value.trim();
    const customerPhone = document.getElementById('modalCustomerPhone').value.trim();
    const customerEmail = document.getElementById('modalCustomerEmail').value.trim();
    const note = document.getElementById('modalBookingNote').value.trim();
    const paymentMethod = document.querySelector('input[name="modalPaymentMethod"]:checked').value;

    if (!customerName) {
        alert("Please enter customer full name!");
        document.getElementById('modalCustomerName').focus();
        return;
    }
    if (!customerPhone) {
        alert("Please enter customer phone number!");
        document.getElementById('modalCustomerPhone').focus();
        return;
    }
    if (selectedSlots.length === 0) {
        alert("Please select at least one court time slot!");
        return;
    }

    const servicesPayload = [];
    facilityProducts.forEach(p => {
        const qty = selectedProductsMap[p.productId] || 0;
        if (qty > 0) {
            servicesPayload.push({
                productId: p.productId,
                quantity: qty,
                price: p.price
            });
        }
    });

    // Check if phone matches an existing Account in system
    let targetAccountId = null;
    if (customerPhone) {
        try {
            const phoneCheckRes = await fetch('/api/owner/booking/check-phone?phone=' + encodeURIComponent(customerPhone));
            if (phoneCheckRes.ok) {
                const phoneCheckData = await phoneCheckRes.json();
                if (phoneCheckData.exists && phoneCheckData.account) {
                    const acc = phoneCheckData.account;
                    const confirmUseAccount = confirm(
                        `The phone number [${customerPhone}] matches an existing registered account:\n` +
                        `• Account Name: ${acc.fullName || 'Customer'}\n` +
                        `• Email: ${acc.email || 'N/A'}\n\n` +
                        `Would you like to associate this reservation directly with this user's account?`
                    );
                    if (confirmUseAccount) {
                        targetAccountId = acc.id;
                        if (!customerName || customerName === "Khách vãng lai" || customerName === "Walk-in Guest") {
                            customerName = acc.fullName;
                        }
                    }
                }
            }
        } catch (e) {
            console.warn("Could not check phone account:", e);
        }
    }

    const payload = {
        facilityId: currentFacilityId,
        facilitySportId: currentFacilitySportId,
        bookingDate: currentDate,
        customerName: customerName,
        customerPhone: customerPhone,
        customerEmail: customerEmail,
        targetAccountId: targetAccountId,
        note: note,
        paymentMethod: paymentMethod,
        slots: selectedSlots,
        services: servicesPayload
    };

    const btnSubmit = document.getElementById('btnSubmitOnSiteBooking');
    if (btnSubmit) {
        btnSubmit.disabled = true;
        btnSubmit.innerHTML = `<div class="animate-spin rounded-full h-4 w-4 border-b-2 border-slate-950"></div> <span>Processing...</span>`;
    }

    try {
        const res = await fetch('/api/owner/booking/create-onsite', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const contentType = res.headers.get("content-type");
        let result = {};
        if (contentType && contentType.includes("application/json")) {
            result = await res.json();
        } else {
            throw new Error(`Server returned HTTP ${res.status}`);
        }

        if (res.ok && result.success) {
            if (result.paymentMethod === 'VNPAY' && result.paymentUrl) {
                alert("🎉 On-site reservation created successfully!\nRedirecting to VNPay payment gateway...");
                window.location.href = result.paymentUrl;
            } else {
                alert("🎉 On-site reservation completed successfully!");
                window.closeOnSiteBookingModal();
                window.clearSlotSelection();
                selectedProductsMap = {};
                document.getElementById('modalCustomerName').value = '';
                document.getElementById('modalCustomerPhone').value = '';
                document.getElementById('modalCustomerEmail').value = '';
                document.getElementById('modalBookingNote').value = '';
                loadTimelineData();
            }
        } else {
            alert("Reservation Error: " + (result.message || "Failed to complete transaction."));
        }
    } catch (e) {
        console.error("On-site booking error:", e);
        alert("Connection or data processing error: " + (e.message || "Please try again."));
    } finally {
        if (btnSubmit) {
            btnSubmit.disabled = false;
            btnSubmit.innerHTML = `<span>Confirm Reservation</span><svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"/></svg>`;
        }
    }
};

// --- BOOKING DETAIL SIDE PANEL ---
window.fetchAndShowBookingDetail = async function (bookingId) {
    if (!bookingId) return;

    const panel = document.getElementById('bookingDetailPanel');
    const content = document.getElementById('bookingDetailContent');

    if (!panel || !content) return;

    panel.classList.remove('hidden');
    content.innerHTML = `<div class="p-6 text-center text-slate-400 flex items-center justify-center gap-2">
        <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div> Đang tải thông tin...
    </div>`;

    try {
        const res = await fetch(`/api/owner/booking/detail/${bookingId}`);
        if (!res.ok) throw new Error("Failed to fetch booking detail");

        const data = await res.json();
        renderBookingDetailCard(data);
    } catch (e) {
        console.error(e);
        content.innerHTML = `<div class="p-4 text-center text-red-500 font-semibold">Không thể tải thông tin đơn hàng #${bookingId}.</div>`;
    }
};

window.closeBookingDetailPanel = function () {
    const panel = document.getElementById('bookingDetailPanel');
    if (panel) panel.classList.add('hidden');
};

function renderBookingDetailCard(data) {
    const content = document.getElementById('bookingDetailContent');
    if (!content) return;

    const isConfirmed = data.bookingStatus === 'CONFIRMED';
    const isPaid = data.paymentStatus === 'PAID';
    const isPartial = data.paymentStatus === 'PARTIAL';

    content.innerHTML = `
        <!-- Section 1: Header Status -->
        <div class="bg-white p-3.5 rounded-xl border border-slate-200 space-y-2 shadow-2xs">
            <div class="flex justify-between items-center">
                <span class="font-extrabold text-sm text-slate-800">Đơn Hàng #${data.bookingId}</span>
                <span class="px-2.5 py-0.5 rounded-full text-[11px] font-bold ${isConfirmed ? 'bg-emerald-100 text-emerald-800 border border-emerald-200' : 'bg-amber-100 text-amber-800 border border-amber-200'}">
                    ${isConfirmed ? 'Đã xác nhận' : 'Chờ xử lý'}
                </span>
            </div>
            <div class="text-[11px] text-slate-400 flex justify-between items-center">
                <span>Thời gian tạo:</span>
                <span class="font-medium text-slate-600">${data.createdAt || 'N/A'}</span>
            </div>
        </div>

        <!-- Section 2: Customer Information -->
        <div class="bg-white p-3.5 rounded-xl border border-slate-200 space-y-2 shadow-2xs">
            <div class="font-bold text-[10px] uppercase tracking-wider text-slate-400">Thông Tin Khách Hàng</div>
            <div class="space-y-1.5 text-slate-700">
                <div class="flex items-center gap-2 font-bold text-slate-800 text-xs">
                    <svg class="w-3.5 h-3.5 text-blue-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
                    <span>${data.bookerName}</span>
                </div>
                <div class="flex items-center gap-2 text-slate-500 font-medium">
                    <svg class="w-3.5 h-3.5 text-slate-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/></svg>
                    <span>${data.bookerPhone}</span>
                </div>
                ${data.bookerEmail && data.bookerEmail !== 'N/A' ? `
                    <div class="flex items-center gap-2 text-slate-500 text-[11px]">
                        <svg class="w-3.5 h-3.5 text-slate-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/></svg>
                        <span>${data.bookerEmail}</span>
                    </div>
                ` : ''}
                ${data.note ? `
                    <div class="mt-2 pt-2 border-t border-slate-100 text-[11px] text-amber-700 bg-amber-50 p-2 rounded-lg italic">
                        📝 ${data.note}
                    </div>
                ` : ''}
            </div>
        </div>

        <!-- Section 3: Booked Slots Details -->
        <div class="bg-white p-3.5 rounded-xl border border-slate-200 space-y-2 shadow-2xs">
            <div class="font-bold text-[10px] uppercase tracking-wider text-slate-400">Danh Sách Ca Đặt (${data.groupedSlotBlocks ? data.groupedSlotBlocks.length : 0})</div>
            <div class="space-y-2">
                ${data.groupedSlotBlocks && data.groupedSlotBlocks.length > 0 ? data.groupedSlotBlocks.map(b => {
                    const isPending = b.slotStatus === 'PENDING';
                    const isCheckedIn = b.slotStatus === 'CHECKED_IN';
                    const isCheckedOut = b.slotStatus === 'CHECKED_OUT';
                    const slotIdsJson = JSON.stringify(b.slotIds);

                    return `
                    <div class="p-2.5 bg-slate-50 rounded-lg border border-slate-200 space-y-2">
                        <div class="flex justify-between items-center text-slate-700">
                            <div>
                                <div class="font-bold text-xs text-slate-800">⚽ ${b.courtName} (${b.startTime} - ${b.endTime})</div>
                                <div class="text-[10px] text-slate-500 font-medium">Trạng thái: 
                                    <span class="font-bold ${isCheckedOut ? 'text-slate-500' : (isCheckedIn ? 'text-emerald-600' : 'text-amber-600')}">
                                        ${isCheckedOut ? 'Đã Check-out' : (isCheckedIn ? 'Đã Check-in' : 'Chờ Check-in')}
                                    </span>
                                </div>
                            </div>
                            <div class="font-bold text-blue-900 text-xs">${formatVND(b.totalPrice)}</div>
                        </div>
                        
                        <div class="pt-1 flex gap-2">
                            ${isPending ? `
                                <button onclick="handleCheckIn(${slotIdsJson}, ${data.bookingId})" class="w-full py-1.5 px-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-lg shadow-xs transition-colors flex items-center justify-center gap-1">
                                    <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>
                                    <span>Check-in Ca Này</span>
                                </button>
                            ` : ''}
                            ${isCheckedIn ? `
                                <button onclick="openCheckOutModal(${data.bookingId}, ${slotIdsJson})" class="w-full py-1.5 px-3 bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs rounded-lg shadow-xs transition-colors flex items-center justify-center gap-1">
                                    <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/></svg>
                                    <span>Check-out & Thanh Toán</span>
                                </button>
                            ` : ''}
                            ${isCheckedOut ? `
                                <div class="w-full py-1 text-center bg-slate-200 text-slate-600 font-bold text-[11px] rounded-lg">
                                    ✓ Đã Hoàn Tất Ca
                                </div>
                            ` : ''}
                        </div>
                    </div>
                    `;
                }).join('') : `
                    <div class="text-slate-400 italic text-center py-2">Không có ca đặt</div>
                `}
            </div>
        </div>

        <!-- Section 4: Accompanying Services -->
        <div class="bg-white p-3.5 rounded-xl border border-slate-200 space-y-2 shadow-2xs">
            <div class="font-bold text-[10px] uppercase tracking-wider text-slate-400">Dịch Vụ Đi Kèm (${data.services ? data.services.length : 0})</div>
            ${!data.services || data.services.length === 0 ? `
                <div class="text-slate-400 italic text-center py-2">Không có dịch vụ đi kèm</div>
            ` : `
                <div class="space-y-1.5">
                    ${data.services.map(s => `
                        <div class="flex justify-between items-center text-slate-700 p-2 bg-emerald-50/50 rounded-lg border border-emerald-100/60">
                            <div>
                                <div class="font-bold text-xs text-slate-800">${s.productName}</div>
                                <div class="text-[10px] text-slate-500">${formatVND(s.unitPrice)} x ${s.quantity} ${s.unit ? s.unit : ''}</div>
                            </div>
                            <div class="font-bold text-emerald-700 text-xs">${formatVND(s.totalAmount)}</div>
                        </div>
                    `).join('')}
                </div>
            `}
        </div>

        <!-- Section 5: Payment Summary -->
        <div class="bg-slate-900 text-white p-4 rounded-xl space-y-2 shadow-md">
            <div class="flex justify-between text-xs text-slate-300">
                <span>Tiền sân:</span>
                <span>${formatVND(data.courtAmount || 0)}</span>
            </div>
            <div class="flex justify-between text-xs text-slate-300">
                <span>Tiền dịch vụ:</span>
                <span>${formatVND(data.productAmount || 0)}</span>
            </div>
            <div class="pt-2 border-t border-slate-800 flex justify-between items-center font-extrabold text-sm">
                <span>Tổng cộng:</span>
                <span class="text-emerald-400 text-base">${formatVND(data.totalAmount || 0)}</span>
            </div>
            <div class="flex justify-between items-center text-xs pt-1 border-t border-slate-800/60">
                <span class="text-slate-400">Đã thanh toán:</span>
                <span class="font-bold text-emerald-400">${formatVND(data.paidAmount || 0)}</span>
            </div>
            <div class="flex justify-between items-center text-xs font-bold text-red-400 bg-red-950/60 p-2 rounded-lg border border-red-800/40">
                <span>Còn thiếu:</span>
                <span>${formatVND(data.remainingAmount || 0)}</span>
            </div>
            <div class="flex justify-between items-center text-[11px] pt-1">
                <span class="text-slate-400">Trạng thái TT:</span>
                <span class="font-bold ${isPaid ? 'text-emerald-400' : (isPartial ? 'text-amber-400' : 'text-red-400')}">
                    ${isPaid ? 'Đã thanh toán đủ' : (isPartial ? 'Thanh toán 1 phần' : 'Chưa thanh toán')}
                </span>
            </div>
        </div>
    `;
}

// --- CHECK-IN & CHECK-OUT SETTLEMENT JS HANDLERS ---

window.handleCheckIn = async function (slotIds, bookingId) {
    if (!slotIds || slotIds.length === 0) return;
    try {
        const res = await fetch('/api/owner/booking/checkin', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ slotIds: slotIds })
        });
        const result = await res.json();
        if (res.ok && result.success) {
            alert(result.message || "Check-in thành công!");
            fetchAndShowBookingDetail(bookingId);
            loadTimelineData();
        } else {
            alert("Lỗi khi Check-in: " + (result.message || "Thao tác thất bại."));
        }
    } catch (e) {
        console.error("Check-in error:", e);
        alert("Lỗi kết nối khi Check-in.");
    }
};

let currentCheckoutData = null;

window.openCheckOutModal = async function (bookingId, slotIds) {
    try {
        const res = await fetch(`/api/owner/booking/detail/${bookingId}`);
        if (!res.ok) throw new Error("Failed to fetch booking detail");
        const data = await res.json();
        currentCheckoutData = { bookingId, slotIds, data };

        document.getElementById('coBookingId').innerText = '#' + bookingId;
        document.getElementById('coCourtAmount').innerText = formatVND(data.courtAmount || 0);
        document.getElementById('coProductAmount').innerText = formatVND(data.productAmount || 0);
        document.getElementById('coTotalAmount').innerText = formatVND(data.totalAmount || 0);
        document.getElementById('coPaidAmount').innerText = formatVND(data.paidAmount || 0);
        document.getElementById('coRemainingAmount').innerText = formatVND(data.remainingAmount || 0);

        const modal = document.getElementById('modalCheckOutSettlement');
        if (modal) modal.classList.remove('hidden');
    } catch (e) {
        console.error("Error opening checkout modal:", e);
        alert("Không thể tải thông tin quyết toán.");
    }
};

window.closeCheckOutModal = function () {
    const modal = document.getElementById('modalCheckOutSettlement');
    if (modal) modal.classList.add('hidden');
    currentCheckoutData = null;
};

window.confirmCheckOutSettlement = async function () {
    if (!currentCheckoutData) return;
    const { bookingId, slotIds } = currentCheckoutData;
    const paymentMethod = document.getElementById('coPaymentMethod') ? document.getElementById('coPaymentMethod').value : 'CASH';

    const btn = document.getElementById('btnConfirmCheckOut');
    if (btn) {
        btn.disabled = true;
        btn.innerText = "Đang xử lý...";
    }

    try {
        const res = await fetch('/api/owner/booking/checkout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                bookingId: bookingId,
                slotIds: slotIds,
                paymentMethod: paymentMethod
            })
        });
        const result = await res.json();
        if (res.ok && result.success) {
            alert(result.message || "Check-out & Quyết toán thành công!");
            closeCheckOutModal();
            fetchAndShowBookingDetail(bookingId);
            loadTimelineData();
        } else {
            alert("Lỗi khi Check-out: " + (result.message || "Thao tác thất bại."));
        }
    } catch (e) {
        console.error("Checkout settlement error:", e);
        alert("Đã xảy ra lỗi kết nối khi quyết toán.");
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerText = "Xác Nhận Check-out";
        }
    }
};

