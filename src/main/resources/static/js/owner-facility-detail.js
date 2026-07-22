document.addEventListener("DOMContentLoaded", function () {
    if (typeof facilityId !== 'undefined' && facilityId > 0) {
        fetchFacilityDetail(facilityId);
    }
});

let facilityData = null;
let activeSportIndex = 0;
let galleryImagesList = [];
let currentImageIndex = 0;

function fetchFacilityDetail(id) {
    return fetch(`/api/owner/facilities/${id}`)
        .then(response => {
            if (!response.ok) throw new Error("Network response was not ok");
            return response.json();
        })
        .then(data => {
            facilityData = data.data ? data.data : data;
            renderFacilityDetail(facilityData);
            loadFacilityStaff(id);
        })
        .catch(error => {
            console.error('Error:', error);
            const container = document.getElementById('overviewCard');
            if (container) container.innerHTML = `<div class="text-red-500 p-4">Lỗi tải dữ liệu: ${error.message}</div>`;
        });
}

function renderFacilityDetail(data) {
    // Alert visibility
    const alertBox = document.getElementById('pendingAlert');
    if (alertBox) {
        if (data.approvalStatus === 'PENDING') {
            alertBox.classList.remove('hidden');
        } else {
            alertBox.classList.add('hidden');
        }
    }

    // Overview Card
    const openTimeStr = data.openTime ? data.openTime.substring(0, 5) : '00:00';
    const closeTimeStr = data.closeTime ? data.closeTime.substring(0, 5) : '00:00';
    const lat = data.latitude || '21.0189';
    const lng = data.longitude || '105.7634';
    const totalSports = data.sports ? data.sports.length : 0;

    let statusBadge = '';
    if (data.isActive !== false) {
        statusBadge = `<span class="bg-green-100 text-green-700 text-[10px] font-extrabold px-3 py-1 rounded-full border border-green-200 tracking-wider">${i18nDetail.statusOpen}</span>`;
    } else {
        statusBadge = `<span class="bg-gray-100 text-gray-700 text-[10px] font-extrabold px-3 py-1 rounded-full border border-gray-200 tracking-wider">${i18nDetail.statusClosed}</span>`;
    }

    document.getElementById('overviewCard').innerHTML = `
        <div>
            <div class="flex items-center gap-3 mb-3">
                <h2 class="text-2xl font-bold text-gray-900">${data.name || 'N/A'}</h2>
                ${statusBadge}
            </div>
            <p class="text-sm text-gray-500 mb-6 flex items-start gap-1.5">
                <svg class="w-4 h-4 flex-shrink-0 text-blue-500 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                ${data.fullAddress || data.address || 'Chưa cập nhật địa chỉ'}
            </p>
            <p class="text-sm text-gray-600 leading-relaxed line-clamp-3 mb-8">
                ${data.description || ''}
            </p>
        </div>
        <div class="bg-gray-50 rounded-2xl p-5 grid grid-cols-1 md:grid-cols-3 gap-4 border border-gray-100">
            <div>
                <p class="text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">${i18nDetail.openTime}</p>
                <p class="text-sm font-bold text-gray-900 flex items-center gap-1.5">
                    <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                    ${openTimeStr} - ${closeTimeStr}
                </p>
            </div>
            <div>
                <p class="text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">${i18nDetail.coordinates}</p>
                <p class="text-sm font-bold text-gray-900">${lat}, ${lng}</p>
            </div>
            <div>
                <p class="text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">${i18nDetail.totalSports}</p>
                <p class="text-sm font-bold text-gray-900">${totalSports} <span class="font-medium text-gray-500">${i18nDetail.sportsUnit}</span></p>
            </div>
        </div>
    `;

    // Images Card
    galleryImagesList = data.galleryImages || [];
    currentImageIndex = 0;
    renderImagesCard();

    renderSportTabs();
}

function renderSportTabs() {
    const container = document.getElementById('sportsTabsContainer');
    let html = '';

    if (facilityData.sports && facilityData.sports.length > 0) {
        facilityData.sports.forEach((sport, index) => {
            const isActive = index === activeSportIndex;
            const classes = isActive
                ? 'bg-blue-600 text-white shadow-md shadow-blue-200'
                : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-50';
            html += `<button class="px-5 py-2.5 rounded-full text-sm font-bold transition-all ${classes}" onclick="switchSport(${index})">${sport.sportName}</button>`;
        });
    }
    const addTabClasses = (activeSportIndex === -1)
        ? 'bg-blue-600 text-white shadow-md shadow-blue-200 border border-blue-600'
        : 'text-blue-600 bg-white border border-blue-200 hover:bg-blue-50 border-dashed';
    html += `<button class="px-5 py-2.5 rounded-full text-sm font-bold transition-all flex items-center gap-1.5 justify-center ${addTabClasses}" onclick="renderAddSportForm()"><svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path></svg>${i18nDetail.tabAddSport.replace(/^\\+\\s*/, '')}</button>`;

    container.innerHTML = html;

    if (activeSportIndex === -1) {
        document.getElementById('sportStatusContainer').classList.add('hidden');
    } else if (facilityData.sports && facilityData.sports.length > 0) {
        document.getElementById('sportStatusContainer').classList.remove('hidden');
        renderActiveSportDetails();
    } else {
        document.getElementById('sportStatusContainer').classList.add('hidden');
        document.getElementById('sportDetailContent').innerHTML = `<div class="text-center py-10 text-gray-500">Chưa có cấu hình môn thể thao.</div>`;
    }
}

function switchSport(index) {
    activeSportIndex = index;
    renderSportTabs();
}

function renderAddSportForm() {
    activeSportIndex = -1;
    renderSportTabs(); // highlight add tab

    // Fetch master sports
    fetch('/api/public/sports')
        .then(res => res.json())
        .then(data => {
            const allSports = data.data || [];

            // Filter out already added sports
            const currentSportIds = facilityData.sports ? facilityData.sports.map(s => s.sportId) : [];
            const availableSports = allSports.filter(s => !currentSportIds.includes(s.sportId));

            if (availableSports.length === 0) {
                document.getElementById('sportDetailContent').innerHTML = `<div class="text-center py-10 text-gray-500 font-medium">Tất cả các môn thể thao đã được kích hoạt cho cơ sở này.</div>`;
                return;
            }

            let options = '';
            availableSports.forEach(s => {
                options += `<option value="${s.sportId}" data-dur="${s.defaultMinDurationMinutes}" data-step="${s.defaultSlotStepMinutes}">${s.sportName}</option>`;
            });

            let html = `
                <div class="max-w-xl mx-auto bg-white rounded-3xl p-8 border border-gray-100 shadow-sm mt-4">
                    <h3 class="text-base font-bold text-gray-900 mb-6 flex items-center justify-center gap-2">
                        <svg class="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                        ${i18nDetail.addSportTitle}
                    </h3>
                    
                    <form id="addSportForm" class="space-y-6">
                        <div>
                            <label class="block text-[11px] font-bold text-gray-400 uppercase tracking-wide mb-2">${i18nDetail.addSportChoose}</label>
                            <select id="newSportId" name="sportId" required class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4 appearance-none">
                                ${options}
                            </select>
                        </div>
                        
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label class="block text-[11px] font-bold text-gray-400 uppercase tracking-wide mb-2">${i18nDetail.addSportMinDur}</label>
                                <select id="newMinDur" name="minDurationMinutes" required class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4 appearance-none">
                                    <option value="30">30 minutes</option>
                                    <option value="45">45 minutes</option>
                                    <option value="60" selected>60 minutes</option>
                                    <option value="90">90 minutes</option>
                                    <option value="120">120 minutes</option>
                                </select>
                            </div>
                            <div>
                                <label class="block text-[11px] font-bold text-gray-400 uppercase tracking-wide mb-2">${i18nDetail.addSportSlotStep}</label>
                                <select id="newSlotStep" name="slotStepMinutes" required class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4 appearance-none">
                                    <option value="30" selected>30 minutes</option>
                                    <option value="60">60 minutes</option>
                                </select>
                            </div>
                        </div>
                        
                        <div class="pt-4 flex justify-center gap-3">
                            <button type="button" onclick="switchSport(0)" class="px-6 py-2.5 rounded-full text-sm font-bold text-gray-700 bg-white border border-gray-200 hover:bg-gray-50 transition">${i18nDetail.addSportBtnCancel}</button>
                            <button type="submit" class="px-6 py-2.5 rounded-full text-sm font-bold text-white bg-blue-600 hover:bg-blue-700 shadow-md shadow-blue-200 transition">${i18nDetail.addSportBtnActivate}</button>
                        </div>
                    </form>
                </div>
            `;
            document.getElementById('sportDetailContent').innerHTML = html;

            // Set default values based on first option
            const selectEl = document.getElementById('newSportId');
            if (selectEl && selectEl.options.length > 0) {
                const opt = selectEl.options[selectEl.selectedIndex];
                if (opt.dataset.dur) document.getElementById('newMinDur').value = opt.dataset.dur;
                if (opt.dataset.step) document.getElementById('newSlotStep').value = opt.dataset.step;
            }

            // Change default values when sport changes
            selectEl.addEventListener('change', function () {
                const opt = this.options[this.selectedIndex];
                if (opt.dataset.dur) document.getElementById('newMinDur').value = opt.dataset.dur;
                if (opt.dataset.step) document.getElementById('newSlotStep').value = opt.dataset.step;
            });

            // Handle submit
            document.getElementById('addSportForm').addEventListener('submit', function (e) {
                e.preventDefault();
                const formData = new FormData(this);
                const data = Object.fromEntries(formData.entries());
                data.sportId = parseInt(data.sportId);
                data.minDurationMinutes = parseInt(data.minDurationMinutes);
                data.slotStepMinutes = parseInt(data.slotStepMinutes);

                fetch('/api/owner/facilities/' + facilityId + '/sports', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                })
                    .then(res => {
                        if (!res.ok) throw new Error(i18nDetail.addSportFail);
                        showCustomAlert(i18nDetail.addSportSuccess, 'success', () => {
                            fetchFacilityDetail(facilityId);
                        });
                    })
                    .catch(err => showCustomAlert(err.message, 'error'));
            });
        });
}

function renderActiveSportDetails() {
    const sport = facilityData.sports[activeSportIndex];
    if (!sport) return;

    // Update Top Right Sport Status Badge
    const badgeContainer = document.getElementById('sportStatusBadge');
    if (sport.isActive !== false) {
        badgeContainer.innerHTML = `<span class="w-2 h-2 rounded-full bg-green-500 shadow-[0_0_0_2px_rgba(34,197,94,0.2)]"></span><span class="text-green-600">${i18nDetail.sportStatusActive}</span>`;
        badgeContainer.className = "ml-2 flex items-center gap-2 px-3 py-1.5 rounded-full border border-green-200 bg-green-50";
    } else {
        badgeContainer.innerHTML = `<span class="w-2 h-2 rounded-full bg-gray-400"></span><span class="text-gray-600">${i18nDetail.sportStatusInactive}</span>`;
        badgeContainer.className = "ml-2 flex items-center gap-2 px-3 py-1.5 rounded-full border border-gray-200 bg-gray-50";
    }

    let html = '';

    // Section A: Config
    const hasPriceRules = sport.priceRules && sport.priceRules.length > 0;

    html += `
        <div id="sportConfigSection" class="relative bg-slate-50/50 p-6 rounded-2xl border border-slate-100">
            <div class="flex justify-between items-center mb-4">
                <h3 class="text-base font-bold text-gray-900 flex items-center gap-2">
                    ${i18nDetail.sectionConfig} <span class="text-gray-400 font-normal">(${sport.sportName})</span>
                </h3>
                <button onclick="toggleEditSportConfig()" id="btnEditSportConfig" class="text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 shadow-sm px-3 py-1.5 rounded-lg flex items-center hover:-translate-y-0.5 hover:shadow-md transition-all duration-200"><svg class="w-3.5 h-3.5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>Edit</button>
            </div>
            
            <div id="sportConfigDisplay" class="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div>
                    <label class="block text-xs font-bold text-gray-400 uppercase tracking-wide mb-2">${i18nDetail.configName}</label>
                    <input type="text" readonly value="${sport.sportName}" class="w-full rounded-xl border-gray-200 bg-gray-50 shadow-sm text-sm py-3 px-4 text-gray-700">
                </div>
                <div>
                    <label class="block text-xs font-bold text-gray-400 uppercase tracking-wide mb-2">${i18nDetail.configMinDuration}</label>
                    <input type="text" readonly value="${sport.minDurationMinutes || 30} minutes" class="w-full rounded-xl border-gray-200 bg-gray-50 shadow-sm text-sm py-3 px-4 text-gray-700">
                </div>
                <div>
                    <label class="block text-xs font-bold text-gray-400 uppercase tracking-wide mb-2">${i18nDetail.configSlotStep}</label>
                    <input type="text" readonly value="${sport.slotStepMinutes || 30} minutes" class="w-full rounded-xl border-gray-200 bg-gray-50 shadow-sm text-sm py-3 px-4 text-gray-700">
                </div>
            </div>
            
            <form id="sportConfigEditForm" class="hidden space-y-4" onsubmit="submitEditSportConfig(event, ${sport.facilitySportId}, ${sport.sportId})">
                <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                    <div>
                        <label class="block text-xs font-bold text-gray-400 uppercase tracking-wide mb-2">${i18nDetail.configName}</label>
                        <input type="text" readonly value="${sport.sportName}" class="w-full rounded-xl border-gray-200 bg-gray-50 shadow-sm text-sm py-3 px-4 text-gray-700">
                    </div>
                    <div>
                        <label class="block text-xs font-bold text-gray-400 uppercase tracking-wide mb-2">${i18nDetail.configMinDuration}</label>
                        <select name="minDurationMinutes" required class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4 appearance-none">
                            <option value="30" ${sport.minDurationMinutes == 30 ? 'selected' : ''}>30 minutes</option>
                            <option value="45" ${sport.minDurationMinutes == 45 ? 'selected' : ''}>45 minutes</option>
                            <option value="60" ${sport.minDurationMinutes == 60 ? 'selected' : ''}>60 minutes</option>
                            <option value="90" ${sport.minDurationMinutes == 90 ? 'selected' : ''}>90 minutes</option>
                            <option value="120" ${sport.minDurationMinutes == 120 ? 'selected' : ''}>120 minutes</option>
                        </select>
                    </div>
                    <div>
                        <label class="block text-xs font-bold text-gray-400 uppercase tracking-wide mb-2 flex items-center gap-1">${i18nDetail.configSlotStep}
                            ${hasPriceRules ? '<div class="relative group inline-block ml-1 cursor-help"><svg class="w-4 h-4 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg><div class="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 hidden group-hover:block w-48 bg-gray-900 text-white text-xs font-normal rounded p-2 z-10 text-center shadow-lg">Cannot change slot step when pricing rules exist. Please delete all pricing rules first.</div></div>' : ''}
                        </label>
                        <select id="slotStepMinutesSelect" required class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4 appearance-none ${hasPriceRules ? 'opacity-60 cursor-not-allowed bg-gray-100' : ''}" ${hasPriceRules ? 'disabled' : ''}>
                            <option value="30" ${sport.slotStepMinutes == 30 ? 'selected' : ''}>30 minutes</option>
                            <option value="60" ${sport.slotStepMinutes == 60 ? 'selected' : ''}>60 minutes</option>
                        </select>
                        ${hasPriceRules ? `<input type="hidden" id="slotStepMinutesHidden" value="${sport.slotStepMinutes}">` : ''}
                    </div>
                </div>
                <div class="flex justify-end gap-3 mt-4">
                    <button type="button" onclick="toggleEditSportConfig()" class="px-5 py-2 rounded-full text-sm font-bold text-gray-700 bg-white border border-gray-200 hover:bg-gray-50 transition">Cancel</button>
                    <button type="submit" class="px-5 py-2 rounded-full text-sm font-bold text-white bg-blue-600 hover:bg-blue-700 shadow-md shadow-blue-200 transition">Save configuration</button>
                </div>
            </form>
        </div>
    `;

    // Section B: Courts
    const courts = sport.courts || [];
    html += `
        <div class="bg-slate-50/50 p-6 rounded-2xl border border-slate-100">
            <div class="flex justify-between items-center mb-4">
                <h3 class="text-base font-bold text-gray-900 flex items-center gap-2">
                    ${i18nDetail.sectionCourts} <span class="text-gray-400 font-normal">(${courts.length} sân)</span>
                </h3>
                <button class="text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 shadow-sm px-3 py-1.5 rounded-lg flex items-center hover:-translate-y-0.5 hover:shadow-md transition-all duration-200" onclick="renderAddCourtForm()"><svg class="w-3.5 h-3.5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path></svg>${i18nDetail.btnAddCourt.replace(/^\\+\\s*/, '')}</button>
            </div>
            <div id="addCourtFormContainer"></div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
    `;

    if (courts.length > 0) {
        courts.forEach(court => {
            const courtStatusBadge = court.isActive !== false
                ? `<span class="text-[10px] font-extrabold text-green-600 bg-green-50 px-2.5 py-1 rounded-full border border-green-100">${i18nDetail.courtActive}</span>`
                : `<span class="text-[10px] font-extrabold text-gray-500 bg-gray-100 px-2.5 py-1 rounded-full border border-gray-200">${i18nDetail.courtInactive}</span>`;

            const toggleIcon = court.isActive !== false
                ? `<label class="relative inline-flex items-center cursor-pointer" title="Toggle Court">
                     <input type="checkbox" class="sr-only peer" checked onchange="toggleCourt(${court.courtId})">
                     <div class="w-9 h-5 bg-gray-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-green-500"></div>
                   </label>`
                : `<label class="relative inline-flex items-center cursor-pointer" title="Toggle Court">
                     <input type="checkbox" class="sr-only peer" onchange="toggleCourt(${court.courtId})">
                     <div class="w-9 h-5 bg-gray-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-green-500"></div>
                   </label>`;

            let attributesHtml = '';
            if (court.attributes && court.attributes.length > 0) {
                attributesHtml = '<div class="flex flex-wrap gap-2 mb-6">';
                court.attributes.forEach(attr => {
                    let val = attr.value;
                    if (attr.dataType === 'BOOLEAN') val = val === 'true' ? 'Có' : 'Không';
                    attributesHtml += `<span class="text-[10px] font-semibold text-blue-600 bg-blue-50 px-2 py-1 rounded-md">${attr.attributeName}: ${val}</span>`;
                });
                attributesHtml += '</div>';
            } else {
                attributesHtml = '<div class="mb-6"></div>';
            }

            html += `
                <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 flex flex-col justify-between">
                    <div>
                        <div class="flex justify-between items-start mb-2">
                            <h4 class="font-bold text-gray-900">${court.courtName}</h4>
                        </div>
                        <p class="text-xs text-gray-500 mb-4">${court.description || 'Không có mô tả'}</p>
                        ${attributesHtml}
                    </div>
                    <div class="flex justify-between items-center pt-3 border-t border-gray-50">
                        <div class="flex items-center gap-3">
                            ${toggleIcon}
                            ${courtStatusBadge}
                        </div>
                        <div class="flex gap-3">
                            <button onclick="renderEditCourtForm(${court.courtId})" class="text-gray-400 hover:text-blue-500 transition focus:outline-none" title="Chỉnh sửa sân">
                                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                            </button>
                            <button onclick="deleteCourt(${court.courtId})" class="text-gray-400 hover:text-red-500 transition focus:outline-none" title="Xóa sân">
                                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                            </button>
                        </div>
                    </div>
                </div>
            `;
        });
    } else {
        html += `<div class="col-span-full text-sm text-gray-500 italic text-center py-4">No courts yet.</div>`;
    }
    html += `</div></div>`;

    // Section C: Pricing
    const prices = (sport.priceRules || []).filter(rule => rule.isActive !== false);
    html += `
        <div class="bg-slate-50/50 p-6 rounded-2xl border border-slate-100">
            <div class="flex justify-between items-center mb-4">
                <h3 class="text-base font-bold text-gray-900 flex items-center gap-2">
                    ${i18nDetail.sectionPricing} <span class="text-[10px] font-normal text-gray-400 uppercase tracking-wider">${i18nDetail.pricingNote}</span>
                </h3>
                <div class="flex items-center gap-2">
                    <button onclick="renderPricingConfigForm(${sport.facilitySportId}, '${sport.sportName}', ${sport.slotStepMinutes || 30})" class="text-xs font-bold text-blue-700 bg-blue-50 border border-blue-200 hover:bg-blue-100 shadow-sm px-3 py-1.5 rounded-lg flex items-center hover:-translate-y-0.5 transition-all duration-200">
                        <svg class="w-3.5 h-3.5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 002-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>Smart Setup
                    </button>
                    <button onclick="renderAddPricingForm(${sport.facilitySportId}, '${sport.sportName}', ${sport.slotStepMinutes || 30})" class="text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 shadow-sm px-3 py-1.5 rounded-lg flex items-center hover:-translate-y-0.5 hover:shadow-md transition-all duration-200"><svg class="w-3.5 h-3.5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path></svg>${i18nDetail.btnAddPricing.replace(/^\\+\\s*/, '')}</button>
                </div>
            </div>
            
            <div id="addPricingFormContainer"></div>
            <div id="pricingConfigFormContainer"></div>
            
            <div id="pricingTableContainer" class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
                <table class="w-full text-sm text-left">
                    <thead class="text-xs text-gray-400 uppercase tracking-wider bg-gray-50/50 border-b border-gray-100">
                        <tr>
                            <th class="px-6 py-4 font-bold">${i18nDetail.pricingDayType}</th>
                            <th class="px-6 py-4 font-bold">${i18nDetail.pricingTimeFrame}</th>
                            <th class="px-6 py-4 font-bold">${i18nDetail.pricingPrice}</th>
                            <th class="px-6 py-4 font-bold text-center">${i18nDetail.pricingAction}</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-gray-50">
    `;

    if (prices.length > 0) {
        prices.forEach(rule => {
            let dayTypeStr = 'Không xác định';
            let dayTypeBadge = '';
            if (rule.dayType === 'WEEKDAY') {
                dayTypeStr = i18nDetail.pricingWeekday;
                dayTypeBadge = 'text-blue-700 bg-blue-50 border-blue-100';
            } else if (rule.dayType === 'WEEKEND') {
                dayTypeStr = i18nDetail.pricingWeekend;
                dayTypeBadge = 'text-orange-700 bg-orange-50 border-orange-100';
            }

            const startStr = rule.startTime ? rule.startTime.substring(0, 5) : '';
            const endStr = rule.endTime ? rule.endTime.substring(0, 5) : '';

            html += `
                <tr class="hover:bg-gray-50/50 transition">
                    <td class="px-6 py-4">
                        <span class="text-xs font-bold px-3 py-1.5 rounded-full border ${dayTypeBadge}">${dayTypeStr}</span>
                    </td>
                    <td class="px-6 py-4 font-medium text-gray-700 flex items-center gap-2">
                        <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                        ${startStr} - ${endStr}
                    </td>
                    <td class="px-6 py-4 font-black text-blue-600">
                        ${new Intl.NumberFormat('vi-VN').format(rule.pricePerSlot)} đ
                    </td>
                    <td class="px-6 py-4 text-center flex justify-center gap-2">
                        <button onclick="editPriceRule(${rule.priceRuleId}, ${sport.facilitySportId}, '${sport.sportName}', ${sport.slotStepMinutes || 30})" class="text-gray-300 hover:text-blue-500 transition focus:outline-none" title="Edit">
                            <svg class="w-4 h-4 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                        </button>
                        <button onclick="deletePriceRule(${rule.priceRuleId})" class="text-gray-300 hover:text-red-500 transition focus:outline-none" title="Delete">
                            <svg class="w-4 h-4 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                        </button>
                    </td>
                </tr>
            `;
        });
    } else {
        html += `<tr><td colspan="4" class="px-6 py-8 text-center text-gray-500 italic">No pricing rules yet.</td></tr>`;
    }

    html += `</tbody></table></div></div>`;

    document.getElementById('sportDetailContent').innerHTML = html;
}

function cancelAddCourt() {
    document.getElementById('addCourtFormContainer').innerHTML = '';
}

function renderAddCourtForm() {
    const sport = facilityData.sports[activeSportIndex];
    if (!sport) return;

    // Fetch attributes
    fetch('/api/public/sports/' + sport.sportId + '/attributes')
        .then(res => res.json())
        .then(data => {
            const attributes = data.data || [];

            let attrsHtml = '';
            attributes.forEach(attr => {
                let inputHtml = '';
                if (attr.dataType === 'SELECT') {
                    let options = [];
                    try { options = JSON.parse(attr.optionsJson); } catch (e) { }
                    let optHtml = options.map(o => `<option value="${o}">${o}</option>`).join('');
                    inputHtml = `<select name="attr_${attr.attributeId}" ${attr.isRequired ? 'required' : ''} class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4 appearance-none">${optHtml}</select>`;
                } else if (attr.dataType === 'BOOLEAN') {
                    inputHtml = `<select name="attr_${attr.attributeId}" ${attr.isRequired ? 'required' : ''} class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4 appearance-none"><option value="true">Có</option><option value="false">Không</option></select>`;
                } else if (attr.dataType === 'NUMBER') {
                    inputHtml = `<input type="number" name="attr_${attr.attributeId}" ${attr.isRequired ? 'required' : ''} class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4">`;
                } else {
                    inputHtml = `<input type="text" name="attr_${attr.attributeId}" ${attr.isRequired ? 'required' : ''} class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4">`;
                }

                attrsHtml += `
                    <div>
                        <label class="block text-[11px] font-bold text-gray-400 uppercase tracking-wide mb-2">${attr.attributeName} ${attr.isRequired ? '<span class="text-red-500">*</span>' : ''}</label>
                        ${inputHtml}
                    </div>
                `;
            });

            let formHtml = `
                <div class="bg-gray-50/50 rounded-2xl p-6 border border-gray-200 mb-6 shadow-sm">
                    <h4 class="text-sm font-bold text-gray-900 mb-4">${i18nDetail.addCourtTitle} ${sport.sportName}</h4>
                    <form id="addCourtForm" class="space-y-4">
                        <div>
                            <label class="block text-[11px] font-bold text-gray-400 uppercase tracking-wide mb-2">${i18nDetail.addCourtName} <span class="text-red-500">*</span></label>
                            <input type="text" name="courtName" required placeholder="${i18nDetail.addCourtNamePlaceholder}" class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4">
                        </div>
                        <div>
                            <label class="block text-[11px] font-bold text-gray-400 uppercase tracking-wide mb-2">${i18nDetail.addCourtDesc}</label>
                            <input type="text" name="description" placeholder="${i18nDetail.addCourtDescPlaceholder}" class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4">
                        </div>
                        
                        ${attrsHtml}
                        
                        <div class="pt-2 flex justify-end gap-3">
                            <button type="button" onclick="cancelAddCourt()" class="px-5 py-2 rounded-full text-sm font-bold text-gray-700 bg-white border border-gray-200 hover:bg-gray-50 transition">${i18nDetail.addCourtBtnCancel}</button>
                            <button type="submit" class="px-5 py-2 rounded-full text-sm font-bold text-white bg-blue-600 hover:bg-blue-700 shadow-md shadow-blue-200 transition">${i18nDetail.addCourtBtnConfirm}</button>
                        </div>
                    </form>
                </div>
            `;

            const container = document.getElementById('addCourtFormContainer');
            container.innerHTML = formHtml;
            container.scrollIntoView({ behavior: 'smooth', block: 'center' });

            document.getElementById('addCourtForm').addEventListener('submit', function (e) {
                e.preventDefault();
                const formData = new FormData(this);
                const data = Object.fromEntries(formData.entries());

                let courtAttributes = [];
                for (const key in data) {
                    if (key.startsWith('attr_')) {
                        courtAttributes.push({
                            attributeId: parseInt(key.replace('attr_', '')),
                            value: data[key]
                        });
                        delete data[key];
                    }
                }

                const requestPayload = {
                    facilitySportId: sport.facilitySportId,
                    courtName: data.courtName,
                    description: data.description,
                    attributes: courtAttributes
                };

                fetch('/api/owner/courts', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(requestPayload)
                })
                    .then(res => {
                        if (!res.ok) throw new Error(i18nDetail.addCourtFail);
                        showCustomAlert(i18nDetail.addCourtSuccess, 'success', () => {
                            fetchFacilityDetail(facilityId);
                        });
                    })
                    .catch(err => showCustomAlert(err.message, 'error'));
            });
        });
}

function toggleCourt(courtId) {
    fetch('/api/owner/courts/' + courtId + '/toggle', { method: 'PUT' })
        .then(res => {
            if (!res.ok) throw new Error('Cannot toggle court status');
            showCustomAlert('Court status toggled successfully!', 'success', () => fetchFacilityDetail(facilityId));
        })
        .catch(err => showCustomAlert(err.message, 'error'));
}

function deleteCourt(courtId) {
    if (typeof showCustomConfirm === 'function') {
        showCustomConfirm('Are you sure you want to delete this court? (It will be permanently deleted if there are no bookings)', () => {
            performDelete(courtId);
        });
    } else {
        if (confirm('Are you sure you want to delete this court? (It will be permanently deleted if there are no bookings)')) {
            performDelete(courtId);
        }
    }
}

function performDelete(courtId) {
    fetch('/api/owner/courts/' + courtId, { method: 'DELETE' })
        .then(async res => {
            if (res.status === 409) {
                const data = await res.json();
                if (data.message === 'COURT_HAS_BOOKINGS') {
                    if (typeof showCustomConfirm === 'function') {
                        showCustomConfirm('This court has bookings or dependencies so it cannot be permanently deleted. Would you like to deactivate it instead?', () => {
                            performForceDeactivate(courtId);
                        });
                    } else {
                        if (confirm('This court has bookings or dependencies so it cannot be permanently deleted. Would you like to deactivate it instead?')) {
                            performForceDeactivate(courtId);
                        }
                    }
                } else {
                    showCustomAlert(data.message || 'Data error', 'error');
                }
                return;
            }
            if (!res.ok) throw new Error('Cannot delete court');
            showCustomAlert('Court deleted successfully!', 'success', () => fetchFacilityDetail(facilityId));
        })
        .catch(err => {
            if (err) showCustomAlert(err.message, 'error');
        });
}

function performForceDeactivate(courtId) {
    fetch('/api/owner/courts/' + courtId + '?forceDeactivate=true', { method: 'DELETE' })
        .then(r => {
            if (!r.ok) throw new Error('Cannot deactivate court');
            showCustomAlert('Court deactivated successfully!', 'success', () => fetchFacilityDetail(facilityId));
        })
        .catch(err => showCustomAlert(err.message, 'error'));
}

function renderEditCourtForm(courtId) {
    const sport = facilityData.sports[activeSportIndex];
    if (!sport || !sport.courts) return;

    const court = sport.courts.find(c => c.courtId === courtId);
    if (!court) return;

    fetch('/api/public/sports/' + sport.sportId + '/attributes')
        .then(res => res.json())
        .then(data => {
            const attributes = data.data || [];

            let attrsHtml = '';
            attributes.forEach(attr => {
                // Find existing value
                const existingAttr = (court.attributes || []).find(a => a.attributeName === attr.attributeName); // The backend returns attributeName instead of attributeId in CourtAttributeDTO, wait, CourtAttributeDTO has attributeName and value. But to update we need attributeId. Wait, we have it if it's the same schema? Let's check CourtAttributeDTO.
                // Wait, if existingAttr only has attributeName and value, how do we match? By attributeName!
                const val = existingAttr ? existingAttr.value : '';

                let inputHtml = '';
                if (attr.dataType === 'SELECT') {
                    let options = [];
                    try { options = JSON.parse(attr.optionsJson); } catch (e) { }
                    let optHtml = options.map(o => `<option value="${o}" ${o === val ? 'selected' : ''}>${o}</option>`).join('');
                    inputHtml = `<select name="attr_${attr.attributeId}" ${attr.isRequired ? 'required' : ''} class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4 appearance-none">${optHtml}</select>`;
                } else if (attr.dataType === 'BOOLEAN') {
                    inputHtml = `<select name="attr_${attr.attributeId}" ${attr.isRequired ? 'required' : ''} class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4 appearance-none"><option value="true" ${val === 'true' ? 'selected' : ''}>Có</option><option value="false" ${val === 'false' ? 'selected' : ''}>Không</option></select>`;
                } else if (attr.dataType === 'NUMBER') {
                    inputHtml = `<input type="number" name="attr_${attr.attributeId}" value="${val}" ${attr.isRequired ? 'required' : ''} class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4">`;
                } else {
                    inputHtml = `<input type="text" name="attr_${attr.attributeId}" value="${val}" ${attr.isRequired ? 'required' : ''} class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4">`;
                }

                attrsHtml += `
                    <div>
                        <label class="block text-[11px] font-bold text-gray-400 uppercase tracking-wide mb-2">${attr.attributeName} ${attr.isRequired ? '<span class="text-red-500">*</span>' : ''}</label>
                        ${inputHtml}
                    </div>
                `;
            });

            let formHtml = `
                <div class="bg-blue-50/50 rounded-2xl p-6 border border-blue-100 mb-6 shadow-sm">
                    <h4 class="text-sm font-bold text-blue-900 mb-4">Sửa thông tin sân: ${court.courtName}</h4>
                    <form id="editCourtForm" class="space-y-4">
                        <div>
                            <label class="block text-[11px] font-bold text-gray-400 uppercase tracking-wide mb-2">${i18nDetail.addCourtName} <span class="text-red-500">*</span></label>
                            <input type="text" name="courtName" value="${court.courtName}" required placeholder="${i18nDetail.addCourtNamePlaceholder}" class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4">
                        </div>
                        <div>
                            <label class="block text-[11px] font-bold text-gray-400 uppercase tracking-wide mb-2">${i18nDetail.addCourtDesc}</label>
                            <input type="text" name="description" value="${court.description || ''}" placeholder="${i18nDetail.addCourtDescPlaceholder}" class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4">
                        </div>
                        
                        ${attrsHtml}
                        
                        <div class="pt-2 flex justify-end gap-3">
                            <button type="button" onclick="cancelAddCourt()" class="px-5 py-2 rounded-full text-sm font-bold text-gray-700 bg-white border border-gray-200 hover:bg-gray-50 transition">${i18nDetail.addCourtBtnCancel}</button>
                            <button type="submit" class="px-5 py-2 rounded-full text-sm font-bold text-white bg-blue-600 hover:bg-blue-700 shadow-md shadow-blue-200 transition">Cập nhật sân</button>
                        </div>
                    </form>
                </div>
            `;

            document.getElementById('addCourtFormContainer').innerHTML = formHtml;
            document.getElementById('addCourtFormContainer').scrollIntoView({ behavior: 'smooth' });

            document.getElementById('editCourtForm').addEventListener('submit', function (e) {
                e.preventDefault();
                const formData = new FormData(this);
                const data = Object.fromEntries(formData.entries());

                let courtAttributes = [];
                for (const key in data) {
                    if (key.startsWith('attr_')) {
                        courtAttributes.push({
                            attributeId: parseInt(key.replace('attr_', '')),
                            value: data[key]
                        });
                        delete data[key];
                    }
                }

                const requestPayload = {
                    courtName: data.courtName,
                    description: data.description,
                    attributes: courtAttributes
                };

                fetch('/api/owner/courts/' + courtId, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(requestPayload)
                })
                    .then(res => {
                        if (!res.ok) throw new Error('Không thể cập nhật sân');
                        showCustomAlert('Cập nhật thông tin sân thành công!', 'success', () => {
                            fetchFacilityDetail(facilityId);
                        });
                    })
                    .catch(err => showCustomAlert(err.message, 'error'));
            });
        });
}

function closeEditGeneralInfoForm() {
    document.getElementById('editGeneralInfoModalContainer').innerHTML = '';
}

function renderEditGeneralInfoForm() {
    if (!facilityData) return;

    let timeOptions = '';
    for (let i = 0; i < 24; i++) {
        const hour = i.toString().padStart(2, '0');
        const ampm = i < 12 ? 'AM' : 'PM';
        const displayHour = i % 12 === 0 ? 12 : i % 12;
        const displayHourStr = displayHour.toString().padStart(2, '0');

        timeOptions += `<option value="${hour}:00:00">${displayHourStr}:00 ${ampm}</option>`;
        timeOptions += `<option value="${hour}:30:00">${displayHourStr}:30 ${ampm}</option>`;
    }

    const modalHtml = `
        <div class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 sm:p-6">
            <div class="bg-white w-full max-w-4xl rounded-3xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
                <div class="px-8 py-6 border-b border-gray-100 flex justify-between items-start relative">
                    <div>
                        <h2 class="text-2xl font-bold text-gray-900">Edit General Information</h2>
                        <p class="text-sm text-gray-500 mt-1">Update basic information of the sports facility.</p>
                    </div>
                    <button onclick="closeEditGeneralInfoForm()" class="w-8 h-8 flex items-center justify-center rounded-full bg-gray-100 text-gray-500 hover:bg-gray-200 hover:text-gray-700 transition absolute top-6 right-6 focus:outline-none">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                    </button>
                </div>

                <div class="p-8 overflow-y-auto custom-scrollbar flex-1">
                    <form id="editGeneralInfoForm" class="space-y-6">
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label class="block text-xs font-bold text-gray-700 uppercase tracking-wide mb-2">Facility Name <span class="text-red-500">*</span></label>
                                <input type="text" name="name" value="${facilityData.name || ''}" required class="w-full rounded-xl border-gray-200 bg-gray-50/50 shadow-sm focus:border-blue-500 focus:ring-blue-500 focus:bg-white transition text-sm py-3 px-4">
                            </div>
                            <div>
                                <label class="block text-xs font-bold text-gray-700 uppercase tracking-wide mb-2">Specific Address <span class="text-red-500">*</span></label>
                                <input type="text" id="editAddress" name="address" value="${facilityData.address || ''}" required class="w-full rounded-xl border-gray-200 bg-gray-50/50 shadow-sm focus:border-blue-500 focus:ring-blue-500 focus:bg-white transition text-sm py-3 px-4">
                            </div>
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                            <div>
                                <label class="block text-xs font-bold text-gray-700 uppercase tracking-wide mb-2">Province / City <span class="text-red-500">*</span></label>
                                <select id="editProvince" name="province" required class="w-full rounded-xl border-gray-200 bg-gray-50/50 shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4 appearance-none">
                                    <option value="" disabled selected>-- Select Province / City --</option>
                                </select>
                            </div>
                            <div>
                                <label class="block text-xs font-bold text-gray-700 uppercase tracking-wide mb-2">District <span class="text-red-500">*</span></label>
                                <select id="editDistrict" name="district" required class="w-full rounded-xl border-gray-200 bg-gray-50/50 shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4 appearance-none">
                                    <option value="${facilityData.district || ''}" selected>${facilityData.district || '-- Select District --'}</option>
                                </select>
                            </div>
                            <div>
                                <label class="block text-xs font-bold text-gray-700 uppercase tracking-wide mb-2">Ward / Commune <span class="text-red-500">*</span></label>
                                <select id="editWard" name="ward" required class="w-full rounded-xl border-gray-200 bg-gray-50/50 shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4 appearance-none">
                                    <option value="${facilityData.ward || ''}" selected>${facilityData.ward || '-- Select Ward / Commune --'}</option>
                                </select>
                            </div>
                        </div>

                        <input type="hidden" id="editLatitude" name="latitude" value="${facilityData.latitude || ''}">
                        <input type="hidden" id="editLongitude" name="longitude" value="${facilityData.longitude || ''}">

                        <div class="border border-blue-100 bg-blue-50/30 rounded-2xl p-5 mb-6 flex justify-between items-center">
                            <div class="flex items-center text-blue-600 font-bold text-xs uppercase tracking-wide">
                                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path>
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
                                </svg>
                                <span>Facility Location</span>
                            </div>
                            <button type="button" id="btnOpenEditMapModal" class="text-sm font-bold text-white bg-blue-600 hover:bg-blue-700 px-5 py-2.5 rounded-xl transition shadow-sm">Choose Location on Map</button>
                        </div>

                        <div>
                            <label class="block text-xs font-bold text-gray-700 uppercase tracking-wide mb-2">Detailed Description</label>
                            <textarea name="description" rows="3" class="w-full rounded-xl border-gray-200 bg-gray-50/50 shadow-sm focus:border-blue-500 focus:ring-blue-500 focus:bg-white transition text-sm py-3 px-4">${facilityData.description || ''}</textarea>
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label class="block text-xs font-bold text-gray-700 uppercase tracking-wide mb-2">Open Time <span class="text-red-500">*</span></label>
                                <select id="editOpenTime" name="openTime" required class="w-full rounded-xl border-gray-200 bg-gray-50/50 shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4 appearance-none">
                                    ${timeOptions}
                                </select>
                            </div>
                            <div>
                                <label class="block text-xs font-bold text-gray-700 uppercase tracking-wide mb-2">Close Time <span class="text-red-500">*</span></label>
                                <select id="editCloseTime" name="closeTime" required class="w-full rounded-xl border-gray-200 bg-gray-50/50 shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-3 px-4 appearance-none">
                                    ${timeOptions}
                                </select>
                            </div>
                        </div>
                    </form>
                </div>

                <div class="px-8 py-5 border-t border-gray-100 bg-gray-50 flex justify-end items-center space-x-3">
                    <button onclick="closeEditGeneralInfoForm()" type="button" class="px-6 py-2.5 rounded-xl text-sm font-semibold text-gray-700 bg-white border border-gray-300 hover:bg-gray-50 hover:text-gray-900 transition shadow-sm">Cancel</button>
                    <button type="submit" form="editGeneralInfoForm" class="px-6 py-2.5 rounded-xl text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 shadow-sm hover:shadow transition">Update Information</button>
                </div>
            </div>
        </div>

        <!-- Inner Map Modal -->
        <div id="editMapModal" class="fixed inset-0 z-[110] hidden items-center justify-center bg-slate-900/60 backdrop-blur-sm p-4 sm:p-6">
            <div class="bg-white w-full max-w-5xl rounded-3xl shadow-2xl overflow-hidden flex flex-col h-[80vh]">
                <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50">
                    <h3 class="text-lg font-bold text-gray-900">Choose Location</h3>
                    <button type="button" id="btnCloseEditMapModal" class="w-8 h-8 flex items-center justify-center rounded-full bg-gray-200 text-gray-500 hover:bg-gray-300 hover:text-gray-700 transition">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                    </button>
                </div>
                <div class="flex-1 relative flex flex-col">
                    <div class="absolute top-4 left-4 right-4 z-[400] flex shadow-md rounded-xl overflow-hidden">
                        <input type="text" id="editMapSearchInput" placeholder="Search a place..." class="flex-1 border-none py-3 px-4 focus:ring-0 text-sm">
                        <button type="button" id="btnEditMapSearch" class="bg-blue-600 text-white px-6 font-bold hover:bg-blue-700 transition">Search</button>
                    </div>
                    <div id="editMap" class="w-full h-full z-10 flex-1"></div>
                </div>
            </div>
        </div>
    `;

    document.getElementById('editGeneralInfoModalContainer').innerHTML = modalHtml;

    if (facilityData.openTime) document.getElementById('editOpenTime').value = facilityData.openTime;
    if (facilityData.closeTime) document.getElementById('editCloseTime').value = facilityData.closeTime;

    initEditMapAndGeocodingLogic();

    document.getElementById('editGeneralInfoForm').addEventListener('submit', function (e) {
        e.preventDefault();

        const lat = document.getElementById('editLatitude').value;
        const lng = document.getElementById('editLongitude').value;
        if (!lat || !lng) {
            showCustomAlert('Please set the facility location either by typing Address or using "Choose Location on Map" before submitting.', 'warning');
            return;
        }

        const formData = new FormData(this);
        const data = Object.fromEntries(formData.entries());

        fetch('/api/owner/facilities/' + facilityId, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        })
            .then(res => {
                if (!res.ok) throw new Error('Could not update general information');
                closeEditGeneralInfoForm();
                showCustomAlert('General information updated successfully!', 'success', () => {
                    fetchFacilityDetail(facilityId);
                });
            })
            .catch(err => showCustomAlert(err.message, 'error'));
    });
}

function initEditMapAndGeocodingLogic() {
    // 1. Province/District/Ward API Integration
    const provinceSelect = document.getElementById('editProvince');
    const districtSelect = document.getElementById('editDistrict');
    const wardSelect = document.getElementById('editWard');

    fetch('https://provinces.open-api.vn/api/p/')
        .then(res => res.json())
        .then(data => {
            let options = '<option value="" disabled>-- Select Province / City --</option>';
            data.forEach(p => { options += `<option value="${p.name}" data-code="${p.code}">${p.name}</option>`; });
            provinceSelect.innerHTML = options;

            // Set initial value
            fuzzyMatchSelect(provinceSelect, facilityData.province, () => {
                const selectedCode = provinceSelect.options[provinceSelect.selectedIndex]?.getAttribute('data-code');
                if (selectedCode) fetchEditDistricts(selectedCode, facilityData.district, facilityData.ward);
            });
        });

    provinceSelect.addEventListener('change', function () {
        const selectedCode = this.options[this.selectedIndex].getAttribute('data-code');
        fetchEditDistricts(selectedCode, null, null);
        wardSelect.innerHTML = '<option value="" disabled selected>-- Select Ward / Commune --</option>';
    });

    districtSelect.addEventListener('change', function () {
        const selectedCode = this.options[this.selectedIndex].getAttribute('data-code');
        fetchEditWards(selectedCode, null);
    });

    function fetchEditDistricts(provinceCode, defaultDistrict, defaultWard) {
        if (!provinceCode) return;
        fetch(`https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`)
            .then(res => res.json())
            .then(data => {
                let options = '<option value="" disabled>-- Select District --</option>';
                data.districts.forEach(d => { options += `<option value="${d.name}" data-code="${d.code}">${d.name}</option>`; });
                districtSelect.innerHTML = options;

                if (defaultDistrict) {
                    fuzzyMatchSelect(districtSelect, defaultDistrict, () => {
                        const dCode = districtSelect.options[districtSelect.selectedIndex]?.getAttribute('data-code');
                        if (dCode) fetchEditWards(dCode, defaultWard);
                    });
                }
            });
    }

    function fetchEditWards(districtCode, defaultWard) {
        if (!districtCode) return;
        fetch(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`)
            .then(res => res.json())
            .then(data => {
                let options = '<option value="" disabled>-- Select Ward / Commune --</option>';
                data.wards.forEach(w => { options += `<option value="${w.name}">${w.name}</option>`; });
                wardSelect.innerHTML = options;

                if (defaultWard) {
                    fuzzyMatchSelect(wardSelect, defaultWard, null);
                }
            });
    }

    function removeAccents(str) {
        return str.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D');
    }

    function fuzzyMatchSelect(selectEl, textToMatch, onSuccess) {
        if (!textToMatch) {
            if (onSuccess) onSuccess();
            return;
        }
        const normalize = (str) => {
            let s = removeAccents(str.toLowerCase());
            s = s.replace(/thanh pho|tinh|quan|huyen|thi xa|phuong|xa|thi tran|city|district|ward/g, '');
            return s.replace(/\s+/g, '').trim();
        };
        const target = normalize(textToMatch);
        for (let i = 0; i < selectEl.options.length; i++) {
            const optText = normalize(selectEl.options[i].text);
            if (optText && (optText.includes(target) || target.includes(optText))) {
                selectEl.selectedIndex = i;
                if (onSuccess) onSuccess();
                return;
            }
        }
        if (onSuccess) onSuccess();
    }

    // 2. Map Logic
    let map, marker;
    const mapModal = document.getElementById('editMapModal');
    let mapInitialized = false;

    document.getElementById('btnOpenEditMapModal').addEventListener('click', () => {
        mapModal.classList.remove('hidden');
        mapModal.classList.add('flex');

        if (!mapInitialized) {
            // Load leaflet if not loaded (or just init if already in head)
            if (typeof L === 'undefined') {
                const cssNode = document.createElement("link");
                cssNode.rel = "stylesheet";
                cssNode.href = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css";
                document.head.appendChild(cssNode);

                const scriptNode = document.createElement("script");
                scriptNode.src = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js";
                scriptNode.onload = initOSM;
                document.head.appendChild(scriptNode);
            } else {
                initOSM();
            }
            mapInitialized = true;
        } else {
            setTimeout(() => { map.invalidateSize(); }, 100);
        }
    });

    document.getElementById('btnCloseEditMapModal').addEventListener('click', () => {
        mapModal.classList.add('hidden');
        mapModal.classList.remove('flex');
    });

    function initOSM() {
        let lat = parseFloat(document.getElementById('editLatitude').value) || 10.762622;
        let lng = parseFloat(document.getElementById('editLongitude').value) || 106.660172;

        map = L.map('editMap').setView([lat, lng], 14);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '© OpenStreetMap'
        }).addTo(map);

        marker = L.marker([lat, lng], { draggable: true }).addTo(map);

        marker.on('dragend', function (event) {
            const pos = event.target.getLatLng();
            reverseGeocode(pos.lat, pos.lng);
        });

        map.on('click', function (e) {
            marker.setLatLng(e.latlng);
            reverseGeocode(e.latlng.lat, e.latlng.lng);
        });

        setTimeout(() => { map.invalidateSize(); }, 100);
    }

    document.getElementById('btnEditMapSearch').addEventListener('click', () => {
        const query = document.getElementById('editMapSearchInput').value;
        if (!query) return;
        fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=1`)
            .then(res => res.json())
            .then(data => {
                if (data && data.length > 0) {
                    const lat = parseFloat(data[0].lat);
                    const lon = parseFloat(data[0].lon);
                    map.setView([lat, lon], 16);
                    marker.setLatLng([lat, lon]);
                } else {
                    showCustomAlert('Location not found', 'warning');
                }
            });
    });

    function reverseGeocode(lat, lng) {
        document.getElementById('editLatitude').value = lat.toFixed(6);
        document.getElementById('editLongitude').value = lng.toFixed(6);

        fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`)
            .then(res => res.json())
            .then(data => {
                if (data && data.address) {
                    const addr = data.address;
                    let specificAddrArr = [];
                    if (addr.house_number) specificAddrArr.push(addr.house_number);
                    if (addr.road) specificAddrArr.push(addr.road);
                    if (specificAddrArr.length > 0) {
                        document.getElementById('editAddress').value = specificAddrArr.join(' ');
                    }

                    const apiProvince = addr.state || addr.city || addr.province || '';
                    fuzzyMatchSelect(provinceSelect, apiProvince, () => {
                        provinceSelect.dispatchEvent(new Event('change'));
                        setTimeout(() => {
                            const apiDistrict = addr.county || addr.city_district || addr.town || addr.district || '';
                            fuzzyMatchSelect(districtSelect, apiDistrict, () => {
                                districtSelect.dispatchEvent(new Event('change'));
                                setTimeout(() => {
                                    const apiWard = addr.suburb || addr.village || addr.quarter || addr.ward || '';
                                    fuzzyMatchSelect(wardSelect, apiWard, null);
                                }, 800);
                            });
                        }, 800);
                    });
                }

                mapModal.classList.add('hidden');
                mapModal.classList.remove('flex');
                showCustomAlert('Address selected! Form auto-filled.', 'success');
            })
            .catch(e => console.error(e));
    }

    // Forward Geocoding
    const addressFields = ['editAddress', 'editProvince', 'editDistrict', 'editWard'];
    let forwardGeocodeTimeout;
    addressFields.forEach(id => {
        document.getElementById(id).addEventListener('change', () => {
            clearTimeout(forwardGeocodeTimeout);
            forwardGeocodeTimeout = setTimeout(forwardGeocode, 1500);
        });
    });

    function forwardGeocode() {
        const address = document.getElementById('editAddress').value;
        const ward = document.getElementById('editWard').value;
        const district = document.getElementById('editDistrict').value;
        const province = document.getElementById('editProvince').value;

        if (!address || !province || !district || !ward) return;

        const fullAddress = `${address}, ${ward}, ${district}, ${province}, Vietnam`;

        fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(fullAddress)}&limit=1`)
            .then(res => res.json())
            .then(data => {
                if (data && data.length > 0) {
                    const lat = parseFloat(data[0].lat);
                    const lon = parseFloat(data[0].lon);
                    document.getElementById('editLatitude').value = lat.toFixed(6);
                    document.getElementById('editLongitude').value = lon.toFixed(6);
                    if (marker && map) {
                        marker.setLatLng([lat, lon]);
                        map.setView([lat, lon], 16);
                    }
                } else {
                    showCustomAlert('Cannot find coordinates for this manual address. Please click "Choose Location on Map" to pin manually.', 'warning');
                }
            });
    }
}

function renderImagesCard() {
    const container = document.getElementById('imagesCard');
    if (!container) return;

    if (galleryImagesList.length === 0) {
        // Fallback
        galleryImagesList = [{ imageUrl: 'https://images.unsplash.com/photo-1595435742656-5272d0b3fa82?auto=format&fit=crop&q=80', isThumbnail: true }];
    }

    let galleryImg = document.getElementById('galleryMainImage');
    if (!galleryImg) {
        // Initial DOM structure
        const arrowsHtml = `
            <button id="prevGalleryBtn" onclick="prevImage()" class="absolute left-2 top-1/2 -translate-y-1/2 w-8 h-8 flex items-center justify-center bg-black/30 hover:bg-black/50 text-white rounded-full transition focus:outline-none z-10 hidden">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg>
            </button>
            <button id="nextGalleryBtn" onclick="nextImage()" class="absolute right-2 top-1/2 -translate-y-1/2 w-8 h-8 flex items-center justify-center bg-black/30 hover:bg-black/50 text-white rounded-full transition focus:outline-none z-10 hidden">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
            </button>
        `;

        container.innerHTML = `
            <div class="flex justify-between items-center mb-4">
                <h3 class="text-xs font-bold text-gray-400 uppercase tracking-wider">${i18nDetail.imagesTitle || 'IMAGES & SLIDER DETAILS'}</h3>
                <button onclick="renderEditGalleryForm()" class="text-xs font-bold text-blue-600 bg-blue-50 hover:bg-blue-100 px-3 py-1.5 rounded-lg flex items-center transition border border-blue-100 shadow-sm">
                    <svg class="w-3.5 h-3.5 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                    Edit Images
                </button>
            </div>
            <div class="relative flex-1 min-h-[240px] rounded-2xl overflow-hidden bg-gray-100 mb-3 group border border-gray-200 shadow-inner">
                <img id="galleryMainImage" src="" alt="Gallery Image" class="absolute inset-0 w-full h-full object-cover transition-opacity duration-300" onerror="this.src='https://placehold.co/600x400?text=Lỗi+Ảnh';">
                <div id="galleryThumbnailTag" class="absolute top-3 left-3 bg-blue-600 text-white text-[10px] font-bold px-2 py-1 rounded-md uppercase tracking-wider shadow-sm hidden z-10">Thumbnail</div>
                ${arrowsHtml}
                <div id="galleryDotsContainer" class="absolute bottom-3 left-1/2 -translate-x-1/2 flex gap-1.5 z-10"></div>
            </div>
            <p id="galleryStatusText" class="text-[11px] text-center text-gray-400 mt-auto"></p>
        `;
        galleryImg = document.getElementById('galleryMainImage');
    }

    // Update DOM data
    const currentImg = galleryImagesList[currentImageIndex];
    galleryImg.src = currentImg.imageUrl;

    const tagEl = document.getElementById('galleryThumbnailTag');
    if (tagEl) {
        if (currentImg.isThumbnail) {
            tagEl.classList.remove('hidden');
        } else {
            tagEl.classList.add('hidden');
        }
    }

    const prevBtn = document.getElementById('prevGalleryBtn');
    const nextBtn = document.getElementById('nextGalleryBtn');
    if (prevBtn && nextBtn) {
        if (galleryImagesList.length > 1) {
            prevBtn.classList.remove('hidden');
            nextBtn.classList.remove('hidden');
        } else {
            prevBtn.classList.add('hidden');
            nextBtn.classList.add('hidden');
        }
    }

    const dotsContainer = document.getElementById('galleryDotsContainer');
    if (dotsContainer) {
        let dotsHtml = '';
        galleryImagesList.forEach((_, idx) => {
            const isActive = idx === currentImageIndex;
            const widthClass = isActive ? 'w-5 bg-blue-500' : 'w-2 bg-white/60 hover:bg-white/90 cursor-pointer';
            dotsHtml += `<div onclick="setCurrentImage(${idx})" class="h-1.5 rounded-full transition-all ${widthClass}"></div>`;
        });
        dotsContainer.innerHTML = dotsHtml;
    }

    const statusText = document.getElementById('galleryStatusText');
    if (statusText) {
        statusText.textContent = `Showing image ${currentImageIndex + 1} of ${galleryImagesList.length} (Thumbnail & Gallery max 20 images)`;
    }
}

function nextImage() {
    currentImageIndex = (currentImageIndex + 1) % galleryImagesList.length;
    renderImagesCard();
}

function prevImage() {
    currentImageIndex = (currentImageIndex - 1 + galleryImagesList.length) % galleryImagesList.length;
    renderImagesCard();
}

function setCurrentImage(index) {
    currentImageIndex = index;
    renderImagesCard();
}

let pendingUploads = [];
let isUploadPanelOpen = false;

function toggleUploadPanel() {
    isUploadPanelOpen = !isUploadPanelOpen;
    renderEditGalleryForm();
}

function removePendingUpload(index) {
    pendingUploads.splice(index, 1);
    renderEditGalleryForm();
}

function renderEditGalleryForm() {
    const container = document.getElementById('editGalleryModalContainer');
    if (!container) return;

    // Filter images
    const thumbnailImg = galleryImagesList.find(img => img.isThumbnail);
    const galleryImgs = galleryImagesList.filter(img => !img.isThumbnail && img.imageId);

    // Left column HTML: Thumbnail
    let thumbnailHtml = '';
    if (thumbnailImg) {
        thumbnailHtml = `
            <div class="relative rounded-2xl overflow-hidden border border-gray-200 mb-4 bg-gray-50 aspect-square flex items-center justify-center group shadow-sm">
                <img src="${thumbnailImg.imageUrl}" class="w-full h-full object-cover" />
                <div class="absolute top-3 left-3 bg-orange-500 text-white text-[10px] font-bold px-2 py-1 rounded-md uppercase">${i18nDetail.galleryCurrent}</div>
                <div class="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                    <button onclick="deleteFacilityImage(${thumbnailImg.imageId})" class="text-sm bg-red-500 text-white font-bold px-4 py-2 rounded-xl hover:bg-red-600 transition shadow-sm">${i18nDetail.galleryBtnDelete}</button>
                </div>
            </div>
        `;
    } else {
        thumbnailHtml = `
            <div class="relative rounded-2xl overflow-hidden border-2 border-dashed border-gray-300 mb-4 bg-gray-50 aspect-square flex flex-col items-center justify-center text-gray-400">
                <svg class="w-10 h-10 mb-2 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                <span class="text-sm font-medium">${i18nDetail.galleryNoThumbnail}</span>
            </div>
        `;
    }

    // Right column HTML: Grid
    let gridHtml = '';
    const totalSlots = 20;
    const currentCount = galleryImgs.length;

    // Existing images
    galleryImgs.forEach((img, idx) => {
        gridHtml += `
            <div class="relative group rounded-2xl overflow-hidden border border-gray-200 bg-gray-50 aspect-square shadow-sm">
                <img src="${img.imageUrl}" class="w-full h-full object-cover" />
                <span class="absolute top-2 left-2 bg-gray-900/60 backdrop-blur text-white text-xs font-bold px-2 py-1 rounded-lg">#${idx + 1}</span>
                
                <button onclick="deleteFacilityImage(${img.imageId})" class="absolute top-2 right-2 bg-red-500 text-white p-1.5 rounded-full opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-600 shadow-sm z-10">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                </button>
                
                <div class="absolute inset-x-0 bottom-0 p-2 opacity-0 group-hover:opacity-100 transition-opacity bg-gradient-to-t from-black/80 to-transparent">
                    <button onclick="setAsThumbnail(${img.imageId})" class="w-full text-xs bg-orange-500 text-white font-bold py-2 rounded-xl flex items-center justify-center hover:bg-orange-600 transition shadow-sm">
                        <svg class="w-3.5 h-3.5 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"></path></svg>
                        ${i18nDetail.galleryBtnSetThumbnail}
                    </button>
                </div>
            </div>
        `;
    });

    // Pending images
    pendingUploads.forEach((upload, idx) => {
        const isThumbnailAction = !thumbnailImg && idx === 0 ? `<div class="absolute inset-x-0 bottom-0 p-2 bg-gradient-to-t from-black/80 to-transparent"><span class="block w-full text-center text-xs text-white font-bold py-1.5 bg-gray-500/80 backdrop-blur rounded-xl">${i18nDetail.galleryWillBeThumbnail}</span></div>` : '';
        gridHtml += `
            <div class="relative group rounded-2xl overflow-hidden border-2 border-blue-400 bg-blue-50 aspect-square shadow-sm">
                <img src="${upload.previewUrl}" class="w-full h-full object-cover opacity-90" />
                <span class="absolute top-2 left-2 bg-blue-500 text-white text-xs font-bold px-2 py-1 rounded-lg shadow-sm">${i18nDetail.galleryNewBadge}</span>
                <button onclick="removePendingUpload(${idx})" class="absolute top-2 right-2 bg-red-500 text-white p-1.5 rounded-full opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-600 shadow-sm z-10">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                </button>
                ${isThumbnailAction}
            </div>
        `;
    });

    // Empty slots
    const totalRendered = galleryImgs.length + pendingUploads.length;
    const remainingSlots = totalSlots - totalRendered;
    const maxEmptySlotsToShow = Math.max(0, 8 - totalRendered);

    for (let i = 0; i < Math.min(remainingSlots, Math.max(4, maxEmptySlotsToShow)); i++) {
        gridHtml += `
            <div class="rounded-2xl border-2 border-dashed border-gray-200 bg-gray-50 aspect-square flex flex-col items-center justify-center text-gray-300">
                <svg class="w-8 h-8 mb-2 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path></svg>
                ${i === Math.min(remainingSlots, Math.max(4, maxEmptySlotsToShow)) - 1 && remainingSlots > Math.max(4, maxEmptySlotsToShow) ? `<span class="text-xs font-bold text-gray-400">+ ${remainingSlots - i} ${i18nDetail.galleryEmptySlot}</span>` : ''}
            </div>
        `;
    }

    const uploadPanelHtml = isUploadPanelOpen ? `
        <div class="mb-6 p-5 border border-blue-100 bg-blue-50/50 rounded-2xl animate-fade-in-up">
            <div class="flex items-center justify-between mb-4">
                <div class="flex items-center text-sm">
                    <svg class="w-4 h-4 text-gray-500 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4"></path></svg>
                    <span class="text-gray-600 font-medium">${i18nDetail.galleryAddingTo}</span>
                    <select class="ml-2 bg-white border border-gray-200 text-gray-800 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block px-2.5 py-1 font-bold">
                        <option>${i18nDetail.galleryLibrary} (${currentCount + pendingUploads.length}/${totalSlots})</option>
                    </select>
                </div>
            </div>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <!-- Dropzone -->
                <div class="border-2 border-dashed border-blue-200 bg-white rounded-2xl p-6 flex flex-col items-center justify-center text-center cursor-pointer hover:bg-blue-50 transition relative overflow-hidden group">
                    <input type="file" multiple accept="image/*" onchange="handleFileSelect(event)" class="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10" />
                    <div class="w-12 h-12 bg-blue-50 text-blue-600 rounded-full flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
                        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path></svg>
                    </div>
                    <p class="text-sm font-bold text-gray-800">${i18nDetail.galleryDragDrop} <span class="text-blue-600 underline">${i18nDetail.galleryBrowse}</span></p>
                    <p class="text-xs text-gray-400 mt-1">${i18nDetail.galleryMaxSize}</p>
                </div>
                
                <!-- URL input -->
                <div class="flex flex-col justify-center space-y-4">
                    <div>
                        <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">${i18nDetail.galleryPasteUrl}</label>
                        <div class="flex gap-2">
                            <input type="url" id="imageUrlInput" placeholder="https://example.com/image.jpg" class="flex-1 px-4 py-2.5 bg-white border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition text-sm outline-none">
                            <button type="button" onclick="handleAddUrl()" class="px-5 py-2.5 bg-gray-900 text-white rounded-xl text-sm font-bold hover:bg-black transition whitespace-nowrap shadow-sm">${i18nDetail.galleryBtnAdd}</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    ` : '';

    container.innerHTML = `
        <div class="fixed inset-0 z-[60] flex items-center justify-center p-4 sm:p-6">
            <div class="absolute inset-0 bg-gray-900/50 backdrop-blur-sm" onclick="closeEditGalleryForm()"></div>
            
            <div class="bg-white rounded-3xl shadow-2xl w-full max-w-[1000px] flex flex-col h-full max-h-[90vh] relative z-10 animate-fade-in-up overflow-hidden">
                <!-- Header -->
                <div class="px-8 py-5 flex items-center justify-between border-b border-gray-100 bg-white z-20">
                    <div class="flex items-center gap-4">
                        <div class="w-12 h-12 rounded-2xl bg-blue-50/50 flex items-center justify-center text-blue-600 border border-blue-100/50 shadow-sm">
                            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                        </div>
                        <div>
                            <h2 class="text-xl font-extrabold text-gray-900 tracking-tight">${i18nDetail.galleryTitle}</h2>
                            <p class="text-sm text-gray-500 mt-0.5">${i18nDetail.gallerySubtitle}</p>
                        </div>
                    </div>
                    <button type="button" onclick="closeEditGalleryForm()" class="text-gray-400 hover:text-gray-700 hover:bg-gray-100 rounded-full transition p-2">
                        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                    </button>
                </div>
                
                <!-- Body -->
                <div class="flex-1 overflow-y-auto p-8 bg-gray-50/30 custom-scrollbar">
                    <div class="grid grid-cols-1 lg:grid-cols-12 gap-10">
                        
                        <!-- Left Col: Thumbnail -->
                        <div class="lg:col-span-4">
                            <div class="flex items-center justify-between mb-4">
                                <h3 class="text-base font-bold text-gray-800">${i18nDetail.galleryThumbnail}</h3>
                                <span class="bg-blue-50 text-blue-700 border border-blue-100 text-[10px] font-bold px-2 py-1 rounded-md uppercase tracking-wider">${i18nDetail.galleryRequired}</span>
                            </div>
                            
                            ${thumbnailHtml}
                            
                            <p class="text-[11px] text-gray-400 text-center mb-5 italic">${i18nDetail.galleryThumbnailSize}</p>
                            
                            <button onclick="document.getElementById('thumbnailFileInput').click()" class="w-full py-2.5 bg-white border border-gray-200 text-gray-700 rounded-xl text-sm font-bold hover:bg-gray-50 hover:border-gray-300 transition flex items-center justify-center shadow-sm">
                                <svg class="w-4 h-4 mr-2 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
                                ${i18nDetail.galleryBtnChangeThumbnail}
                            </button>
                            <input type="file" id="thumbnailFileInput" accept="image/*" onchange="handleThumbnailSelect(event)" class="hidden" />
                        </div>
                        
                        <!-- Right Col: Gallery -->
                        <div class="lg:col-span-8">
                            <div class="flex flex-col sm:flex-row sm:items-center justify-between mb-5 gap-3">
                                <div class="flex items-center">
                                    <h3 class="text-base font-bold text-gray-800">${i18nDetail.galleryCollection}</h3>
                                    <span class="text-sm font-medium text-gray-400 ml-2">(${currentCount + pendingUploads.length}/${totalSlots} ${i18nDetail.galleryImagesUnit})</span>
                                </div>
                                <button onclick="toggleUploadPanel()" class="px-5 py-2.5 ${isUploadPanelOpen ? 'bg-gray-200 text-gray-700 hover:bg-gray-300' : 'bg-gray-900 text-white hover:bg-black'} rounded-xl text-sm font-bold transition flex items-center shadow-sm">
                                    ${isUploadPanelOpen ? `
                                        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5"></path></svg>
                                        ${i18nDetail.galleryBtnHidePanel}
                                    ` : `
                                        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
                                        ${i18nDetail.galleryBtnAddNew}
                                    `}
                                </button>
                            </div>
                            
                            ${uploadPanelHtml}
                            
                            <!-- Grid -->
                            <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
                                ${gridHtml}
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Footer -->
                <div class="px-8 py-5 border-t border-gray-100 flex items-center justify-end bg-white z-20">
                    <div class="flex items-center gap-3">
                        <button type="button" onclick="closeEditGalleryForm()" class="px-6 py-2.5 text-sm font-bold text-gray-600 hover:bg-gray-100 rounded-xl transition">${i18nDetail.galleryBtnCancel}</button>
                        <button type="button" onclick="submitGalleryChanges()" id="saveGalleryBtn" class="px-8 py-2.5 bg-blue-600 text-white rounded-xl text-sm font-bold hover:bg-blue-700 transition shadow-md flex items-center disabled:opacity-50 disabled:cursor-not-allowed border border-blue-700">
                            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                            ${i18nDetail.galleryBtnSave} ${pendingUploads.length > 0 ? `(${pendingUploads.length})` : ''}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
}

function closeEditGalleryForm() {
    const container = document.getElementById('editGalleryModalContainer');
    if (container) container.innerHTML = '';
    pendingUploads = [];
    isUploadPanelOpen = false;
}

function resetGalleryChanges() {
    pendingUploads = [];
    renderEditGalleryForm();
}

function handleFileSelect(e) {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    // Check limit
    const galleryImgs = galleryImagesList.filter(img => !img.isThumbnail && img.imageId);
    const available = 20 - (galleryImgs.length + pendingUploads.length);

    let added = 0;
    for (let i = 0; i < files.length; i++) {
        if (added >= available) {
            showCustomAlert(i18nDetail.galleryMsgMaxImages, 'warning');
            break;
        }
        const file = files[i];
        if (file.size > 5 * 1024 * 1024) {
            showCustomAlert('File ' + file.name + ' ' + i18nDetail.galleryMsgFileTooLarge, 'warning');
            continue;
        }
        pendingUploads.push({
            type: 'file',
            data: file,
            previewUrl: URL.createObjectURL(file)
        });
        added++;
    }

    e.target.value = ''; // Reset input
    renderEditGalleryForm();
}

function handleAddUrl() {
    const input = document.getElementById('imageUrlInput');
    const url = input.value.trim();
    if (!url) return;

    const galleryImgs = galleryImagesList.filter(img => !img.isThumbnail && img.imageId);
    if (galleryImgs.length + pendingUploads.length >= 20) {
        showCustomAlert(i18nDetail.galleryMsgMaxImages, 'warning');
        return;
    }

    pendingUploads.push({
        type: 'url',
        data: url,
        previewUrl: url
    });
    input.value = '';
    renderEditGalleryForm();
}

function handleThumbnailSelect(e) {
    const files = e.target.files;
    if (!files || files.length === 0) return;
    const file = files[0];
    if (file.size > 5 * 1024 * 1024) {
        showCustomAlert(i18nDetail.galleryMsgFileTooLarge, 'warning');
        return;
    }

    // We add it to pending uploads and immediately set it as thumbnail in the preview
    // Actually, backend POST creates it. If we don't save, it doesn't upload.
    // Let's just upload it immediately for UX since backend doesn't support batch well.
    const formData = new FormData();
    formData.append('file', file);

    const toastId = showCustomAlert(i18nDetail.galleryMsgUploading, 'info');
    fetch(`/api/owner/facilities/${facilityId}/images`, {
        method: 'POST',
        body: formData
    })
        .then(async res => {
            if (!res.ok) throw new Error(await res.text());
            const newImg = await res.json();
            // Set it as thumbnail
            return fetch(`/api/owner/facilities/${facilityId}/images/${newImg.imageId}/thumbnail`, {
                method: 'PUT'
            });
        })
        .then(async res => {
            if (!res.ok) throw new Error(await res.text());
            showCustomAlert('Đổi ảnh đại diện thành công!', 'success');
            fetchFacilityDetail(facilityId).then(() => renderEditGalleryForm());
        })
        .catch(err => showCustomAlert('Lỗi: ' + err.message, 'error'));
}

async function submitGalleryChanges() {
    if (pendingUploads.length === 0) {
        closeEditGalleryForm();
        return;
    }

    const btn = document.getElementById('saveGalleryBtn');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<svg class="animate-spin w-4 h-4 mr-2 text-white" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg> ' + i18nDetail.galleryMsgSaving;
    btn.disabled = true;

    try {
        for (let upload of pendingUploads) {
            const formData = new FormData();
            if (upload.type === 'file') formData.append('file', upload.data);
            if (upload.type === 'url') formData.append('url', upload.data);

            const res = await fetch(`/api/owner/facilities/${facilityId}/images`, {
                method: 'POST',
                body: formData
            });
            if (!res.ok) throw new Error(await res.text());
        }

        showCustomAlert('Lưu ảnh thành công!', 'success', () => {
            fetchFacilityDetail(facilityId).then(() => closeEditGalleryForm());
        });
    } catch (err) {
        showCustomAlert('Lỗi khi lưu ảnh: ' + err.message, 'error');
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}

function setAsThumbnail(imageId) {
    showCustomConfirm(i18nDetail.galleryMsgThumbnailConfirm, () => {
        // Immediate API call for gallery images
        fetch(`/api/owner/facilities/${facilityId}/images/${imageId}/thumbnail`, {
            method: 'PUT'
        })
            .then(async response => {
                if (!response.ok) throw new Error("Network error");
                showCustomAlert('Đã đổi Ảnh đại diện!', 'success', () => {
                    fetchFacilityDetail(facilityId).then(() => renderEditGalleryForm());
                });
            })
            .catch(err => showCustomAlert(err.message, 'error'));
    }, 'warning');
}

function deleteFacilityImage(imageId) {
    showCustomConfirm(i18nDetail.galleryMsgDeleteConfirm, () => {
        fetch(`/api/owner/facilities/${facilityId}/images/${imageId}`, {
            method: 'DELETE'
        })
            .then(async response => {
                if (!response.ok) throw new Error("Network error");
                showCustomAlert('Đã xóa ảnh!', 'success', () => {
                    fetchFacilityDetail(facilityId).then(() => renderEditGalleryForm());
                });
            })
            .catch(err => showCustomAlert(err.message, 'error'));
    }, 'danger');
}

function cancelAddPricing() {
    const container = document.getElementById('addPricingFormContainer');
    if (container) container.innerHTML = '';
}

function renderAddPricingForm(sportId, sportName, stepMinutes = 30) {
    const container = document.getElementById('addPricingFormContainer');
    if (!container) return;

    // Add margin bottom to separate from table
    container.className = 'mb-6';

    let timeOptionsHtml = '<option value="" disabled selected>Select time</option>';
    for (let h = 0; h < 24; h++) {
        for (let m = 0; m < 60; m += stepMinutes) {
            const hh = h.toString().padStart(2, '0');
            const mm = m.toString().padStart(2, '0');
            const timeStr = `${hh}:${mm}`;
            timeOptionsHtml += `<option value="${timeStr}">${timeStr}</option>`;
        }
    }

    container.innerHTML = `
        <div class="bg-gray-50/50 rounded-2xl p-6 border border-gray-100 relative">
            <h4 class="font-bold text-gray-900 mb-4 text-sm">${i18nDetail.addPricingTitle} ${sportName}</h4>
            
            <form id="addPricingForm" onsubmit="submitPriceRule(event, ${sportId})" class="space-y-5">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
                    
                    <!-- Day Type -->
                    <div>
                        <label class="block text-[11px] font-bold text-gray-400 uppercase tracking-wider mb-1.5">${i18nDetail.addPricingDayType}</label>
                        <select name="dayType" required class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-2.5 px-4 appearance-none">
                            <option value="WEEKDAY">${i18nDetail.addPricingDayTypeWeekday}</option>
                            <option value="WEEKEND">${i18nDetail.addPricingDayTypeWeekend}</option>
                        </select>
                    </div>
                    
                    <!-- Price Per Slot -->
                    <div>
                        <label class="block text-[11px] font-bold text-gray-400 uppercase tracking-wider mb-1.5">${i18nDetail.addPricingPricePerSlot}</label>
                        <input type="number" name="pricePerSlot" required min="0" class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-2.5 px-4" placeholder="100000">
                    </div>
                    
                    <!-- Start Time -->
                    <div>
                        <label class="block text-[11px] font-bold text-gray-400 uppercase tracking-wider mb-1.5">${i18nDetail.addPricingStartTime}</label>
                        <div class="relative">
                            <select name="startTime" required class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-2.5 pl-4 pr-10 appearance-none">
                                ${timeOptionsHtml}
                            </select>
                            <div class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-gray-400">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                            </div>
                        </div>
                    </div>
                    
                    <!-- End Time -->
                    <div>
                        <label class="block text-[11px] font-bold text-gray-400 uppercase tracking-wider mb-1.5">${i18nDetail.addPricingEndTime}</label>
                        <div class="relative">
                            <select name="endTime" required class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-2.5 pl-4 pr-10 appearance-none">
                                ${timeOptionsHtml}
                            </select>
                            <div class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-gray-400">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Effective From -->
                    <div class="hidden">
                        <label class="block text-[11px] font-bold text-gray-400 uppercase tracking-wider mb-1.5">${i18nDetail.addPricingEffectiveFrom}</label>
                        <div class="relative">
                            <input type="date" name="effectiveFrom" required class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-2.5 pl-4 pr-10" value="${new Date().toISOString().split('T')[0]}">
                            <div class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-gray-400">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Effective To -->
                    <div class="hidden">
                        <label class="block text-[11px] font-bold text-gray-400 uppercase tracking-wider mb-1.5">${i18nDetail.addPricingEffectiveTo}</label>
                        <div class="relative">
                            <input type="date" name="effectiveTo" class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-2.5 pl-4 pr-10">
                            <div class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none text-gray-400">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="flex justify-center gap-3 pt-2">
                    <button type="button" onclick="cancelAddPricing()" class="px-5 py-2 text-sm font-semibold text-gray-600 bg-white border border-gray-200 rounded-xl hover:bg-gray-50 hover:text-gray-900 transition">
                        ${i18nDetail.addPricingBtnCancel}
                    </button>
                    <button type="submit" class="px-5 py-2 text-sm font-bold text-white bg-blue-600 rounded-xl hover:bg-blue-700 transition shadow-sm">
                        ${i18nDetail.addPricingBtnSave}
                    </button>
                </div>
            </form>
        </div>
    `;
    container.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

async function submitPriceRule(e, sportId) {
    e.preventDefault();
    const form = e.target;
    const formData = new FormData(form);

    // Convert time HH:mm to HH:mm:00
    let start = formData.get('startTime');
    let end = formData.get('endTime');
    if (start && start.length === 5) start += ":00";
    if (end && end.length === 5) end += ":00";

    const payload = {
        facilitySportId: sportId,
        dayType: formData.get('dayType'),
        pricePerSlot: parseFloat(formData.get('pricePerSlot')),
        startTime: start,
        endTime: end,
        effectiveFrom: formData.get('effectiveFrom')
    };

    const effectiveTo = formData.get('effectiveTo');
    if (effectiveTo) {
        payload.effectiveTo = effectiveTo;
    }

    try {
        const response = await fetch('/api/owner/price-rules', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            cancelAddPricing();
            showCustomAlert(await response.text(), 'success', () => fetchFacilityDetail(facilityId));
        } else {
            let msg = await response.text();
            try {
                const json = JSON.parse(msg);
                msg = json.message || msg;
            } catch (e) { }
            showCustomAlert(msg, 'error');
        }
    } catch (error) {
        showCustomAlert(error.message, 'error');
    }
}

function deletePriceRule(ruleId) {
    showCustomConfirm("Are you sure you want to delete this price rule?", async () => {
        try {
            const response = await fetch('/api/owner/price-rules/' + ruleId, {
                method: 'DELETE'
            });

            if (response.ok) {
                showCustomAlert(await response.text(), 'success', () => fetchFacilityDetail(facilityId));
            } else {
                let msg = await response.text();
                try {
                    const json = JSON.parse(msg);
                    msg = json.message || msg;
                } catch (e) { }
                showCustomAlert(msg, 'error');
            }
        } catch (error) {
            showCustomAlert(error.message, 'error');
        }
    }, "error");
}

function editPriceRule(ruleId, sportId, sportName, stepMinutes) {
    const sport = facilityData.sports.find(s => s.facilitySportId === sportId);
    if (!sport) return;
    const rule = sport.priceRules.find(r => r.priceRuleId === ruleId);
    if (!rule) return;

    renderAddPricingForm(sportId, sportName, stepMinutes);
    const form = document.getElementById('addPricingForm');

    form.previousElementSibling.innerText = "Chỉnh sửa giá cho " + sportName;
    form.setAttribute('onsubmit', `submitEditPriceRule(event, ${ruleId}, ${sportId})`);

    form.querySelector('[name="dayType"]').value = rule.dayType;
    form.querySelector('[name="pricePerSlot"]').value = rule.pricePerSlot;
    form.querySelector('[name="startTime"]').value = rule.startTime ? rule.startTime.substring(0, 5) : '';
    form.querySelector('[name="endTime"]').value = rule.endTime ? rule.endTime.substring(0, 5) : '';
    if (rule.effectiveFrom) {
        form.querySelector('[name="effectiveFrom"]').value = rule.effectiveFrom;
    }
    if (rule.effectiveTo) {
        form.querySelector('[name="effectiveTo"]').value = rule.effectiveTo;
    }

    form.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

async function submitEditPriceRule(e, ruleId, sportId) {
    e.preventDefault();
    const form = e.target;
    const formData = new FormData(form);

    let start = formData.get('startTime');
    let end = formData.get('endTime');
    if (start && start.length === 5) start += ":00";
    if (end && end.length === 5) end += ":00";

    const payload = {
        facilitySportId: sportId,
        dayType: formData.get('dayType'),
        pricePerSlot: parseFloat(formData.get('pricePerSlot')),
        startTime: start,
        endTime: end,
        effectiveFrom: formData.get('effectiveFrom')
    };

    const effectiveTo = formData.get('effectiveTo');
    if (effectiveTo) {
        payload.effectiveTo = effectiveTo;
    }

    try {
        const response = await fetch('/api/owner/price-rules/' + ruleId, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            cancelAddPricing();
            showCustomAlert(await response.text(), 'success', () => fetchFacilityDetail(facilityId));
        } else {
            let msg = await response.text();
            try {
                const json = JSON.parse(msg);
                msg = json.message || msg;
            } catch (e) { }
            showCustomAlert(msg, 'error');
        }
    } catch (error) {
        showCustomAlert(error.message, 'error');
    }
}

// --- SMART PRICING CONFIGURATION ---

let currentPricingConfig = [];

function cancelPricingConfig() {
    const container = document.getElementById('pricingConfigFormContainer');
    if (container) container.innerHTML = '';
    const display = document.getElementById('pricingTableContainer');
    if (display) display.style.display = 'block';
}

function renderPricingConfigForm(sportId, sportName, stepMinutes = 30) {
    const sport = facilityData.sports.find(s => s.facilitySportId === sportId);
    if (!sport) return;

    // Build current config from prices
    const prices = (sport.priceRules || []).filter(rule => rule.isActive !== false);
    const timeSlots = {};
    prices.forEach(rule => {
        const key = rule.startTime + '-' + rule.endTime;
        if (!timeSlots[key]) {
            timeSlots[key] = {
                startTime: rule.startTime ? rule.startTime.substring(0, 5) : '',
                endTime: rule.endTime ? rule.endTime.substring(0, 5) : '',
                weekdayPrice: '',
                weekendPrice: ''
            };
        }
        if (rule.dayType === 'WEEKDAY') timeSlots[key].weekdayPrice = rule.pricePerSlot;
        if (rule.dayType === 'WEEKEND') timeSlots[key].weekendPrice = rule.pricePerSlot;
    });

    currentPricingConfig = Object.keys(timeSlots).sort().map(key => timeSlots[key]);
    if (currentPricingConfig.length === 0) {
        currentPricingConfig.push({ startTime: '', endTime: '', weekdayPrice: '', weekendPrice: '' });
    }

    const display = document.getElementById('pricingTableContainer');
    if (display) display.style.display = 'none';

    renderPricingRows(sportId, sportName, stepMinutes);
    document.getElementById('pricingConfigFormContainer').scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function getPricingTimeOptions(stepMinutes, selectedTime = '') {
    let html = '<option value="" disabled selected>Select time</option>';
    for (let h = 0; h < 24; h++) {
        for (let m = 0; m < 60; m += stepMinutes) {
            const hh = h.toString().padStart(2, '0');
            const mm = m.toString().padStart(2, '0');
            const timeStr = `${hh}:${mm}`;
            const selected = (timeStr === selectedTime) ? 'selected' : '';
            html += `<option value="${timeStr}" ${selected}>${timeStr}</option>`;
        }
    }
    return html;
}

function renderPricingRows(sportId, sportName, stepMinutes) {
    const container = document.getElementById('pricingConfigFormContainer');
    
    let rowsHtml = '';
    currentPricingConfig.forEach((row, index) => {
        rowsHtml += `
            <div class="flex items-start gap-4 mb-1" data-index="${index}">
                <div class="flex-1">
                    <select class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 transition text-sm py-2 px-3" onchange="updatePricingConfig(${index}, 'startTime', this.value)">
                        ${getPricingTimeOptions(stepMinutes, row.startTime)}
                    </select>
                </div>
                <div class="flex-1">
                    <select class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 transition text-sm py-2 px-3" onchange="updatePricingConfig(${index}, 'endTime', this.value)">
                        ${getPricingTimeOptions(stepMinutes, row.endTime)}
                    </select>
                </div>
                <div class="flex-1">
                    <input type="number" min="0" placeholder="Weekday Price" class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 transition text-sm py-2 px-3" value="${row.weekdayPrice}" onchange="updatePricingConfig(${index}, 'weekdayPrice', this.value)">
                </div>
                <div class="flex-1">
                    <input type="number" min="0" placeholder="Weekend Price" class="w-full rounded-xl border-gray-200 bg-white shadow-sm focus:border-blue-500 transition text-sm py-2 px-3" value="${row.weekendPrice}" onchange="updatePricingConfig(${index}, 'weekendPrice', this.value)">
                </div>
                <div class="pt-1">
                    <button type="button" onclick="removePricingRow(${index}, ${sportId}, '${sportName}', ${stepMinutes})" class="text-gray-400 hover:text-red-500 transition p-1">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                    </button>
                </div>
            </div>
            <div id="pricing-error-${index}" class="text-red-500 text-xs mt-0.5 mb-4 hidden font-medium"></div>
        `;
    });

    container.innerHTML = `
        <div class="bg-yellow-50 border border-yellow-200 rounded-xl p-4 mb-4 text-sm text-yellow-800 flex items-start gap-3 shadow-sm">
            <svg class="w-5 h-5 text-yellow-600 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
            <div>
                <strong>Note:</strong> Saving this configuration will <b>overwrite</b> all existing pricing rules for this sport.
            </div>
        </div>
        <div class="bg-gray-50/50 rounded-2xl p-6 border border-gray-100 mb-6 shadow-sm">
            <h4 class="font-bold text-gray-900 mb-4 text-sm">Smart Pricing Configuration: ${sportName}</h4>
            <div class="flex items-center gap-4 mb-2 text-[10px] font-bold text-gray-400 uppercase tracking-wider">
                <div class="flex-1">Start Time</div>
                <div class="flex-1">End Time</div>
                <div class="flex-1">Weekday Price (VND/slot)</div>
                <div class="flex-1">Weekend Price (VND/slot)</div>
                <div class="w-6"></div>
            </div>
            ${rowsHtml}
            
            <button type="button" onclick="addPricingRow(${sportId}, '${sportName}', ${stepMinutes})" class="mt-2 flex items-center gap-2 text-sm text-green-600 border border-green-200 bg-green-50 hover:bg-green-100 px-3 py-1.5 rounded-lg transition font-medium">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path></svg>
                Add time frame
            </button>
            
            <div class="flex justify-end gap-3 pt-6 mt-4 border-t border-gray-100">
                <button type="button" onclick="cancelPricingConfig()" class="px-5 py-2 text-sm font-semibold text-gray-600 bg-white border border-gray-200 rounded-xl hover:bg-gray-50 hover:text-gray-900 transition shadow-sm">
                    Cancel
                </button>
                <button type="button" onclick="savePricingConfig(${sportId})" class="px-5 py-2 text-sm font-bold text-white bg-green-600 rounded-xl hover:bg-green-700 transition shadow-sm flex items-center gap-2">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                    Save Configuration
                </button>
            </div>
        </div>
    `;
}

function updatePricingConfig(index, field, value) {
    currentPricingConfig[index][field] = value;
}

function addPricingRow(sportId, sportName, stepMinutes) {
    currentPricingConfig.push({ startTime: '', endTime: '', weekdayPrice: '', weekendPrice: '' });
    renderPricingRows(sportId, sportName, stepMinutes);
}

function removePricingRow(index, sportId, sportName, stepMinutes) {
    currentPricingConfig.splice(index, 1);
    if (currentPricingConfig.length === 0) {
        currentPricingConfig.push({ startTime: '', endTime: '', weekdayPrice: '', weekendPrice: '' });
    }
    renderPricingRows(sportId, sportName, stepMinutes);
}

async function savePricingConfig(sportId) {
    currentPricingConfig.forEach((_, index) => {
        const errDiv = document.getElementById(`pricing-error-${index}`);
        if (errDiv) { errDiv.classList.add('hidden'); errDiv.innerText = ''; }
    });

    let hasClientError = false;
    const payloadRows = [];

    currentPricingConfig.forEach((row, index) => {
        let errStr = [];
        if (!row.startTime) errStr.push('Start time is required.');
        if (!row.endTime) errStr.push('End time is required.');
        if (!row.weekdayPrice) errStr.push('Weekday price is required.');
        if (!row.weekendPrice) errStr.push('Weekend price is required.');
        if (row.startTime && row.endTime && row.startTime >= row.endTime) errStr.push('Start time must be before end time.');
        if (row.weekdayPrice && parseFloat(row.weekdayPrice) <= 0) errStr.push('Weekday price must be > 0.');
        if (row.weekendPrice && parseFloat(row.weekendPrice) <= 0) errStr.push('Weekend price must be > 0.');

        if (errStr.length > 0) {
            const errDiv = document.getElementById(`pricing-error-${index}`);
            errDiv.innerText = errStr.join(' ');
            errDiv.classList.remove('hidden');
            hasClientError = true;
        } else {
            payloadRows.push({
                startTime: row.startTime + ':00',
                endTime: row.endTime + ':00',
                weekdayPrice: parseFloat(row.weekdayPrice),
                weekendPrice: parseFloat(row.weekendPrice)
            });
        }
    });

    if (hasClientError) return;

    try {
        const response = await fetch('/api/owner/price-rules/batch', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ facilitySportId: sportId, rows: payloadRows })
        });
        
        const resData = await response.json();

        if (response.ok) {
            cancelPricingConfig();
            showCustomAlert(resData.message, 'success', () => fetchFacilityDetail(facilityId));
        } else {
            if (resData.data && Array.isArray(resData.data)) {
                resData.data.forEach(err => {
                    if (err.row !== undefined) {
                        const errDiv = document.getElementById(`pricing-error-${err.row}`);
                        if (errDiv) {
                            errDiv.innerText = err.message;
                            errDiv.classList.remove('hidden');
                        }
                    }
                });
                showCustomAlert("Please fix the errors in the configuration.", 'error');
            } else {
                showCustomAlert(resData.message || 'Error saving configuration', 'error');
            }
        }
    } catch (error) {
        showCustomAlert(error.message, 'error');
    }
}

// ------------------------------------------

window.toggleEditSportConfig = function () {
    const display = document.getElementById('sportConfigDisplay');
    const form = document.getElementById('sportConfigEditForm');
    const btnEdit = document.getElementById('btnEditSportConfig');

    if (display.classList.contains('hidden')) {
        display.classList.remove('hidden');
        form.classList.add('hidden');
        if (btnEdit) btnEdit.style.display = 'flex';
    } else {
        display.classList.add('hidden');
        form.classList.remove('hidden');
        if (btnEdit) btnEdit.style.display = 'none';
    }
}

window.submitEditSportConfig = function (event, facilitySportId, sportId) {
    event.preventDefault();
    const form = event.target;
    const minDur = parseInt(form.minDurationMinutes.value);

    const selectElem = document.getElementById('slotStepMinutesSelect');
    const hiddenElem = document.getElementById('slotStepMinutesHidden');
    let slotStep;
    if (selectElem && !selectElem.disabled) {
        slotStep = parseInt(selectElem.value);
    } else if (hiddenElem) {
        slotStep = parseInt(hiddenElem.value);
    } else {
        slotStep = parseInt(selectElem.value); // fallback
    }

    // Validate client-side
    if (minDur < slotStep || minDur % slotStep !== 0) {
        showCustomAlert("Min duration must be >= slot step and a multiple of slot step.", "error");
        return;
    }

    const payload = {
        sportId: sportId,
        minDurationMinutes: minDur,
        slotStepMinutes: slotStep
    };

    fetch(`/api/owner/facility-sports/${facilitySportId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(async response => {
            if (!response.ok) {
                const err = await response.json().catch(() => ({}));
                throw new Error(err.message || 'Failed to update sport configuration.');
            }
            showCustomAlert('Sport configuration updated successfully.', 'success', () => {
                fetchFacilityDetail(facilityId); // Refresh data
            });
        })
        .catch(error => {
            showCustomAlert(error.message, "error");
        });
}

// ==================== STAFF SECTION ====================
function loadFacilityStaff(fId) {
    fetch(`/api/owner/staff/by-facility/${fId}`)
        .then(res => res.json())
        .then(response => {
            if (response.success) {
                renderStaffSection(response.data || []);
            }
        })
        .catch(err => console.error('Error loading staff:', err));
}

function renderStaffSection(staffList) {
    const container = document.getElementById('staffSectionContainer');
    if (!container) return;

    const sectionTitle = typeof i18nDetail !== 'undefined' && i18nDetail.sectionStaff
        ? i18nDetail.sectionStaff : 'Assigned Staff';
    const noStaffText = typeof i18nDetail !== 'undefined' && i18nDetail.noStaff
        ? i18nDetail.noStaff : 'No staff assigned to this facility yet.';
    const assignBtnText = typeof i18nDetail !== 'undefined' && i18nDetail.btnAssignStaff
        ? i18nDetail.btnAssignStaff : 'Assign Staff';

    let staffCardsHtml = '';
    if (staffList.length === 0) {
        staffCardsHtml = `
            <div class="text-center py-10 text-slate-400">
                <svg class="mx-auto h-12 w-12 mb-3 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path>
                </svg>
                <p class="text-sm font-medium">${noStaffText}</p>
            </div>
        `;
    } else {
        staffCardsHtml = `<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">` +
            staffList.map(staff => `
                <div class="bg-white border border-gray-100 rounded-xl p-4 flex items-center gap-3 hover:shadow-sm transition-shadow">
                    <div class="w-10 h-10 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center font-bold text-sm overflow-hidden flex-shrink-0">
                        ${staff.avatarPath
                    ? `<img src="${staff.avatarPath}" class="w-full h-full object-cover" alt="">`
                    : `<span>${staff.fullName.charAt(0).toUpperCase()}</span>`
                }
                    </div>
                    <div class="min-w-0 flex-1">
                        <div class="font-semibold text-slate-900 text-sm truncate">${staff.fullName}</div>
                        <div class="text-xs text-slate-400 truncate">${staff.email || staff.phone || ''}</div>
                    </div>
                </div>
            `).join('') + `</div>`;
    }

    container.innerHTML = `
        <div class="flex items-center justify-between mb-6">
            <h3 class="text-sm font-black text-slate-900 uppercase tracking-wider flex items-center gap-2">
                <svg class="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
                ${sectionTitle}
                <span class="text-xs font-bold text-slate-400">(${staffList.length})</span>
            </h3>
            <button onclick="openAssignStaffModal()"
                class="px-5 py-2 bg-blue-600 text-white rounded-xl text-sm font-bold flex items-center shadow-sm hover:-translate-y-0.5 hover:shadow-md hover:bg-blue-700 transition-all duration-200">
                <svg class="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
                ${assignBtnText}
            </button>
        </div>
        ${staffCardsHtml}
        
        <!-- Assign Staff Modal Container (will be rendered when opened) -->
        <div id="assignStaffModalContainer"></div>
    `;
}

function openAssignStaffModal() {
    fetch('/api/owner/staff')
        .then(res => res.json())
        .then(response => {
            if (response.success) {
                renderAssignStaffModal(response.data || []);
            }
        })
        .catch(err => console.error('Error fetching staff list for modal:', err));
}

function renderAssignStaffModal(allStaff) {
    let container = document.getElementById('assignStaffModalContainer');
    if (!container) return;

    // Build list items
    let listHtml = '';
    allStaff.forEach(staff => {
        // If assigned to this facility, pre-check it
        const isAssignedToThis = staff.facilityId === facilityId;
        const isAssignedToOther = staff.facilityId && staff.facilityId !== facilityId;

        let labelAddon = '';
        if (isAssignedToOther) {
            labelAddon = `<span class="text-[10px] bg-orange-100 text-orange-600 px-2 py-0.5 rounded ml-2">Assigned to another facility</span>`;
        }

        listHtml += `
            <label class="flex items-center p-3 border border-gray-100 rounded-xl hover:bg-gray-50 cursor-pointer transition mb-2 staff-checkbox-item" data-search="${(staff.fullName + ' ' + staff.email + ' ' + staff.phone).toLowerCase()}">
                <input type="checkbox" class="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500 mr-3 staff-checkbox" value="${staff.staffId}" ${isAssignedToThis ? 'checked' : ''}>
                <div class="flex-1">
                    <div class="text-sm font-semibold text-gray-900">${staff.fullName} ${labelAddon}</div>
                    <div class="text-xs text-gray-500">${staff.email || ''} - ${staff.phone || ''}</div>
                </div>
            </label>
        `;
    });

    if (allStaff.length === 0) {
        listHtml = `<div class="text-center text-sm text-gray-500 py-6">No staff found. Please create staff first.</div>`;
    }

    container.innerHTML = `
        <div class="fixed inset-0 bg-slate-900/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div class="bg-white rounded-3xl w-full max-w-md shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
                <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
                    <h3 class="text-base font-bold text-gray-900">Assign Staff</h3>
                    <button onclick="document.getElementById('assignStaffModalContainer').innerHTML=''" class="text-gray-400 hover:text-gray-700 transition">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                    </button>
                </div>
                
                <div class="p-6 overflow-y-auto flex-1">
                    <div class="mb-4 relative">
                        <input type="text" id="staffSearchInput" placeholder="Search by name, email, phone..." class="w-full rounded-xl border-gray-200 bg-gray-50 shadow-sm focus:border-blue-500 focus:ring-blue-500 transition text-sm py-2.5 pl-10 px-4" onkeyup="filterStaffList()">
                        <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-gray-400">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
                        </div>
                    </div>
                    
                    <div class="max-h-64 overflow-y-auto pr-2 custom-scrollbar" id="staffListContainer">
                        ${listHtml}
                    </div>
                </div>
                
                <div class="px-6 py-4 border-t border-gray-100 flex justify-end gap-3 bg-gray-50/50">
                    <button onclick="document.getElementById('assignStaffModalContainer').innerHTML=''" class="px-5 py-2 text-sm font-semibold text-gray-600 bg-white border border-gray-200 rounded-xl hover:bg-gray-50 hover:-translate-y-0.5 hover:shadow-md hover:text-gray-900 transition-all duration-200">Cancel</button>
                    <button onclick="submitAssignStaff()" class="px-5 py-2 text-sm font-bold text-white bg-blue-600 rounded-xl hover:bg-blue-700 hover:-translate-y-0.5 hover:shadow-md transition-all duration-200">Confirm Assignment</button>
                </div>
            </div>
        </div>
    `;
}

function filterStaffList() {
    const term = document.getElementById('staffSearchInput').value.toLowerCase();
    const items = document.querySelectorAll('.staff-checkbox-item');
    items.forEach(item => {
        const searchData = item.getAttribute('data-search');
        if (searchData.includes(term)) {
            item.style.display = 'flex';
        } else {
            item.style.display = 'none';
        }
    });
}

function submitAssignStaff() {
    const checkedBoxes = document.querySelectorAll('.staff-checkbox:checked');
    const staffIds = Array.from(checkedBoxes).map(cb => parseInt(cb.value));

    fetch('/api/owner/facilities/' + facilityId + '/staff', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ staffIds: staffIds })
    })
        .then(res => {
            if (!res.ok) throw new Error("Lỗi khi gán nhân viên");
            showCustomAlert("Cập nhật danh sách nhân viên phụ trách thành công!", "success", () => {
                document.getElementById('assignStaffModalContainer').innerHTML = '';
                loadFacilityStaff(facilityId);
            });
        })
        .catch(err => showCustomAlert(err.message, "error"));
}

