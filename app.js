// =============================================
// Propridict – Frontend (API-backed)
// =============================================
const API_BASE = 'http://localhost:8080/api';
const USD_RATE = 0.012;

// DOM References
const citySel = document.getElementById('city');
const landmarkSel = document.getElementById('landmark');
const categorySel = document.getElementById('category');
const sizeInput = document.getElementById('size');
const yearInput = document.getElementById('year');
const roadWidthInput = document.getElementById('roadWidth');
const frontageInput = document.getElementById('frontage');
const cornerPlotSel = document.getElementById('cornerPlot');
const predictionForm = document.getElementById('predictionForm');

const resultsCard = document.getElementById('resultsCard');
const errorCard = document.getElementById('errorCard');
const errorText = document.getElementById('errorText');

const priceEl = document.getElementById('priceResult');
const rateEl = document.getElementById('rateResult');
const premiumBadge = document.getElementById('premiumBadge');
const breakdownEl = document.getElementById('breakdown');

const interestInput = document.getElementById('interest');
const tenureInput = document.getElementById('tenure');
const downPctInput = document.getElementById('downPct');
const emiSummary = document.getElementById('emiSummary');

const offersBody = document.getElementById('offersBody');
const offerTypeSel = document.getElementById('offerType');
const offerCustomerSel = document.getElementById('offerCustomer');
const offerSortSel = document.getElementById('offerSort');
const offersNote = document.getElementById('offersNote');

const dutyStateSel = document.getElementById('dutyState');
const dutyOwnerSel = document.getElementById('dutyOwner');
const dutyUsePredSel = document.getElementById('dutyUsePred');
const dutyValueInput = document.getElementById('dutyValue');
const dutyCalcBtn = document.getElementById('dutyCalcBtn');
const dutySummary = document.getElementById('dutySummary');

const kpiPrice = document.getElementById('kpiPrice');
const kpiRate = document.getElementById('kpiRate');
const kpiDuty = document.getElementById('kpiDuty');
const kpiPrincipal = document.getElementById('kpiPrincipal');
const kpiEMI = document.getElementById('kpiEMI');
const kpiBestOffer = document.getElementById('kpiBestOffer');

const contactForm = document.getElementById('contactForm');
const contactName = document.getElementById('contactName');
const contactEmail = document.getElementById('contactEmail');
const contactPhone = document.getElementById('contactPhone');
const contactPurpose = document.getElementById('contactPurpose');
const contactDate = document.getElementById('contactDate');
const contactTime = document.getElementById('contactTime');
const contactMessage = document.getElementById('contactMessage');
const contactConsent = document.getElementById('contactConsent');
const contactStatus = document.getElementById('contactStatus');

const themeToggle = document.getElementById('themeToggle');
const themeLabel = document.getElementById('themeLabel');

// State
let map, marker;
let currentState = { years: [], baseRates: [], adjustedRates: [], baseFutureRate: 0, size: 0, factor: 1.0, totalPriceINR: 0, lat: 0, lng: 0 };
let bestOffer = null;

// Helpers
const clamp = (n, min, max) => Math.min(Math.max(n, min), max);
const formatINR = (val) => `₹${Math.round(val).toLocaleString('en-IN')}`;
const isDark = () => document.documentElement.dataset.theme === 'dark';

function fillDropdown(sel, items, placeholder) {
  sel.innerHTML = `<option value="">${placeholder}</option>`;
  items.forEach(v => sel.insertAdjacentHTML('beforeend', `<option value="${v}">${v}</option>`));
}

// =============================================
// API Helpers
// =============================================
async function apiFetch(url, options = {}) {
  try {
    const res = await fetch(url, {
      headers: { 'Content-Type': 'application/json' },
      ...options
    });
    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.error || `API error: ${res.status}`);
    }
    return data;
  } catch (err) {
    if (err.message.includes('Failed to fetch') || err.message.includes('NetworkError')) {
      throw new Error('Cannot connect to the backend server. Make sure the Spring Boot server is running on port 8080.');
    }
    throw err;
  }
}

// =============================================
// Data Fetching
// =============================================
async function fetchCities() {
  const cities = await apiFetch(`${API_BASE}/properties/cities`);
  fillDropdown(citySel, cities, 'Select City');
}

async function fetchLandmarks(city) {
  const landmarks = await apiFetch(`${API_BASE}/properties/landmarks?city=${encodeURIComponent(city)}`);
  fillDropdown(landmarkSel, landmarks, 'Select Landmark');
}

async function fetchCategories(city, landmark) {
  const categories = await apiFetch(`${API_BASE}/properties/categories?city=${encodeURIComponent(city)}&landmark=${encodeURIComponent(landmark)}`);
  fillDropdown(categorySel, categories, 'Select Category');
}

async function fetchDutyStates() {
  const states = await apiFetch(`${API_BASE}/duty/states`);
  fillDropdown(dutyStateSel, states, 'Select State');
  dutyStateSel.value = "Madhya Pradesh";
}

// =============================================
// Prediction
// =============================================
async function runPrediction() {
  clearError();

  const city = citySel.value;
  const lm = landmarkSel.value;
  const cat = categorySel.value;
  const size = Number(sizeInput.value);
  const year = Number(yearInput.value);

  if (!city || !lm || !cat) return showError('Please select City, Landmark, and Category.');
  if (!Number.isFinite(size) || size <= 0) return showError('Please enter a valid Size (sq ft) greater than 0.');
  if (!Number.isFinite(year) || year < 2025 || year > 2050) return showError('Prediction Year must be between 2025 and 2050.');

  const roadFt = clamp(Number(roadWidthInput.value || 0), 0, 999);
  const frontageFt = clamp(Number(frontageInput.value || 0), 0, 999);
  const isCorner = (cornerPlotSel.value === 'yes');

  try {
    const result = await apiFetch(`${API_BASE}/predict`, {
      method: 'POST',
      body: JSON.stringify({
        city, landmark: lm, category: cat, size, year,
        roadWidth: roadFt, frontage: frontageFt, cornerPlot: isCorner
      })
    });

    // Update state
    currentState = {
      years: result.years,
      baseRates: result.baseRates,
      adjustedRates: result.adjustedRates,
      baseFutureRate: result.baseRate,
      size,
      factor: result.premiumFactor,
      totalPriceINR: result.totalPriceINR,
      lat: result.lat,
      lng: result.lng
    };

    // Render results
    priceEl.innerHTML = `${formatINR(result.totalPriceINR)} <span class="muted">(≈ $${Math.round(result.totalPriceUSD).toLocaleString('en-IN')})</span>`;
    rateEl.textContent = `Base rate: ${formatINR(result.baseRate)}/sq ft • Adjusted rate: ${formatINR(result.adjustedRate)}/sq ft • CAGR (2018→2024): ${(result.cagr * 100).toFixed(2)}%`;
    premiumBadge.textContent = `Premium factor: x${result.premiumFactor.toFixed(2)} (+${result.premiumPct.toFixed(1)}%)`;

    breakdownEl.innerHTML = `
      • Size: <b>${size.toLocaleString('en-IN')}</b> sq ft<br/>
      • Road width: <b>${roadFt} ft</b> • Frontage: <b>${frontageFt} ft</b> • Corner: <b>${isCorner ? 'Yes' : 'No'}</b><br/>
      • City: <b>${city}</b> • Landmark: <b>${lm}</b> • Category: <b>${cat}</b>
    `;

    plotRates(result.years, result.baseRates, result.adjustedRates);
    resultsCard.style.display = 'block';
    updateMarker(result.lat, result.lng, lm, city);

    updateEMIOutputs(result.totalPriceINR);
    renderBankOffers();
    updateDutyUI();

    kpiPrice.textContent = formatINR(result.totalPriceINR);
    kpiRate.textContent = `${formatINR(result.adjustedRate)}/sq ft`;

    prefillConsultationMessage();

  } catch (err) {
    showError(err.message);
  }
}

// =============================================
// Chart
// =============================================
function plotRates(years, baseRates, adjustedRates) {
  const template = isDark() ? 'plotly_dark' : 'plotly_white';
  Plotly.newPlot('priceGraph', [
    {
      x: years, y: baseRates, type: 'scatter', mode: 'lines+markers',
      name: 'Base rate', line: { color: '#4361ee', width: 3 }, fill: 'tozeroy',
      fillcolor: isDark() ? 'rgba(122,162,255,0.12)' : 'rgba(67,97,238,0.12)'
    },
    {
      x: years, y: adjustedRates, type: 'scatter', mode: 'lines+markers',
      name: 'Adjusted (with premiums)', line: { color: '#9b59b6', width: 3 }
    }
  ], {
    title: 'Rate Trend (₹/sq ft)', xaxis: { title: 'Year' }, yaxis: { title: 'Rate (₹/sq ft)' },
    template, hovermode: 'x unified', margin: { t: 40, l: 50, r: 20, b: 40 }, legend: { orientation: 'h', y: -0.2 }
  }, { responsive: true, displaylogo: false });
}

// =============================================
// EMI (client-side, instant feedback)
// =============================================
function updateEMIOutputs(totalPriceINR) {
  let rate = parseFloat(interestInput.value || '0');
  let years = parseInt(tenureInput.value || '0', 10);
  let downPct = parseFloat(downPctInput.value || '0');

  rate = clamp(rate, 0, 100);
  years = clamp(years, 0, 100);
  downPct = clamp(downPct, 0, 100);

  const downPayment = totalPriceINR * (downPct / 100);
  const principal = Math.max(totalPriceINR - downPayment, 0);

  let emi = 0, totalPay = 0, totalInt = 0;
  if (rate > 0 && years > 0 && principal > 0) {
    const r = rate / 12 / 100;
    const n = years * 12;
    const pow = Math.pow(1 + r, n);
    emi = principal * r * pow / (pow - 1);
    totalPay = emi * n;
    totalInt = totalPay - principal;
  }

  emiSummary.innerHTML = `
    Loan principal: <b>${formatINR(principal)}</b> • Monthly EMI: <b>${formatINR(emi)}</b><br/>
    Total interest: <b>${formatINR(totalInt)}</b> • Total payment: <b>${formatINR(totalPay)}</b>
  `;

  kpiPrincipal.textContent = formatINR(principal);
  kpiEMI.textContent = formatINR(emi);
  return { principal, emi };
}

// =============================================
// Bank Offers (from API)
// =============================================
async function renderBankOffers() {
  if (!currentState || !currentState.totalPriceINR || currentState.totalPriceINR <= 0) {
    offersBody.innerHTML = "";
    offersNote.textContent = "Predict price to see personalized bank offers.";
    bestOffer = null;
    kpiBestOffer.textContent = "—";
    return;
  }

  const loanType = offerTypeSel.value;
  const customer = offerCustomerSel.value;
  const sortBy = offerSortSel.value;
  let tenureYears = parseFloat(tenureInput.value || '20');
  tenureYears = Math.max(1, tenureYears);

  const downPct = parseFloat(downPctInput.value || '0');
  const loanAmount = Math.max(0, currentState.totalPriceINR * (1 - downPct / 100));

  if (loanAmount <= 0) {
    offersBody.innerHTML = "";
    offersNote.textContent = "No loan amount (100% down payment). Adjust down payment to see offers.";
    bestOffer = null;
    kpiBestOffer.textContent = "—";
    return;
  }

  try {
    const offers = await apiFetch(
      `${API_BASE}/offers?type=${encodeURIComponent(loanType)}&customer=${encodeURIComponent(customer)}&sort=${sortBy}&loanAmount=${loanAmount}&tenure=${tenureYears}`
    );

    offersBody.innerHTML = offers.map(o => `
      <tr>
        <td><b>${o.bank}</b></td>
        <td>${o.product}</td>
        <td><span class="rate-pill">${o.rateMin.toFixed(2)}% – ${o.rateMax.toFixed(2)}%</span><br><span class="dim">Updated: ${o.lastUpdated}</span></td>
        <td><b>₹${Math.round(o.emiMin).toLocaleString('en-IN')}</b><br><span class="dim">Est. at min rate</span></td>
        <td>${o.tenureUsed} yrs <span class="dim">(max ${o.maxTenure})</span></td>
        <td>₹${Math.round(o.processingFee).toLocaleString('en-IN')} <span class="dim">@ ${o.procPct}%</span></td>
        <td><a class="apply-btn" href="${o.link}" target="_blank" rel="noopener">Apply</a></td>
      </tr>
    `).join('');

    offersNote.innerHTML = `
      Loan amount considered: <b>₹${Math.round(loanAmount).toLocaleString('en-IN')}</b>
      • Tenure: <b>${tenureYears}</b> yrs (bank max applies)
      • Down payment: <b>${downPct}%</b>
    `;

    if (offers.length > 0) {
      const best = offers.reduce((a, b) => a.emiMin < b.emiMin ? a : b);
      bestOffer = { bank: best.bank, emi: Math.round(best.emiMin) };
      kpiBestOffer.textContent = `${best.bank}: ${formatINR(bestOffer.emi)}`;
    } else {
      bestOffer = null;
      kpiBestOffer.textContent = "—";
    }
  } catch (err) {
    console.error('Failed to load bank offers:', err);
    offersNote.textContent = "Failed to load bank offers.";
  }
}

// =============================================
// Stamp Duty
// =============================================
async function updateDutyUI() {
  const state = dutyStateSel.value || "Madhya Pradesh";
  const owner = dutyOwnerSel.value || "Male";
  const usePred = dutyUsePredSel.value === "yes";
  const value = usePred ? currentState.totalPriceINR : Number(dutyValueInput.value || 0);

  if (!value || value <= 0) {
    dutySummary.innerHTML = `<div class="muted">Enter a valid property value or run a prediction.</div>`;
    kpiDuty.textContent = "—";
    return;
  }

  try {
    const res = await apiFetch(`${API_BASE}/duty/calculate`, {
      method: 'POST',
      body: JSON.stringify({ state, ownerType: owner, propertyValue: value })
    });

    dutySummary.innerHTML = `
      <div><b>${res.state}</b> • Owner: <b>${res.ownerType}</b></div>
      <div style="margin-top:6px;">Stamp duty rate: <b>${res.stampDutyPct.toFixed(2)}%</b> • Registration: <b>${res.registrationPct.toFixed(2)}%</b></div>
      <div style="margin-top:6px;">Stamp Duty: <b>${formatINR(res.stampDutyAmount)}</b><br/>Registration: <b>${formatINR(res.registrationAmount)}</b></div>
      <div style="margin-top:8px;">Total Govt. Fees: <b>${formatINR(res.totalFees)}</b></div>
      <div class="muted" style="margin-top:8px;">All-in cost (Value + Fees): <b>${formatINR(res.allInCost)}</b></div>
    `;
    kpiDuty.textContent = formatINR(res.totalFees);
  } catch (err) {
    dutySummary.innerHTML = `<div class="muted">Error calculating duty: ${err.message}</div>`;
    kpiDuty.textContent = "—";
  }
}

// =============================================
// Error helpers
// =============================================
function showError(msg) {
  errorText.textContent = msg || 'Something went wrong.';
  errorCard.style.display = 'block';
  resultsCard.style.display = 'none';
  offersBody.innerHTML = "";
  offersNote.textContent = "Predict price to see personalized bank offers.";
}
function clearError() { errorCard.style.display = 'none'; }

// =============================================
// Map
// =============================================
function initMap() {
  map = L.map('leafletMap').setView([20.5937, 78.9629], 5);
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map);
  setTimeout(() => map.invalidateSize(), 200);
  window.addEventListener('resize', () => map.invalidateSize());
}

function updateMarker(lat, lng, lm, city) {
  if (marker) { map.removeLayer(marker); marker = null; }
  if (lat && lng) {
    marker = L.marker([lat, lng]).addTo(map).bindPopup(`<b>${lm}, ${city}</b>`).openPopup();
    map.setView([lat, lng], 13);
  }
}

function updateMarkerForCity(city) {
  if (marker) { map.removeLayer(marker); marker = null; }
  // City center coordinates for map centering
  const cityCenters = {
    'Mumbai':      [19.0760, 72.8777],
    'Delhi':       [28.6139, 77.2090],
    'Bangalore':   [12.9716, 77.5946],
    'Hyderabad':   [17.3850, 78.4867],
    'Chennai':     [13.0827, 80.2707],
    'Kolkata':     [22.5726, 88.3639],
    'Pune':        [18.5204, 73.8567],
    'Ahmedabad':   [23.0225, 72.5714],
    'Gurgaon':     [28.4595, 77.0266],
    'Noida':       [28.5355, 77.3910],
    'Jaipur':      [26.9124, 75.7873],
    'Lucknow':     [26.8467, 80.9462],
    'Chandigarh':  [30.7333, 76.7794],
    'Kochi':       [9.9312, 76.2673],
    'Surat':       [21.1702, 72.8311],
    'Nagpur':      [21.1458, 79.0882],
    'Coimbatore':  [11.0168, 76.9558],
    'Visakhapatnam': [17.6868, 83.2185],
    'Patna':       [25.6093, 85.1376],
    'Bhopal':      [23.2599, 77.4126],
    'Indore':      [22.7196, 75.8577],
    'Vadodara':    [22.3072, 73.1812],
    'Thiruvananthapuram': [8.5241, 76.9366],
    'Bhubaneswar': [20.2961, 85.8245],
    'Dehradun':    [30.3165, 78.0322],
    'Guwahati':    [26.1445, 91.7362],
    'Ranchi':      [23.3441, 85.3096],
    'Mysore':      [12.2958, 76.6394],
    'Amritsar':    [31.6340, 74.8723],
    'Varanasi':    [25.3176, 82.9739],
    'Agra':        [27.1767, 78.0081]
  };
  if (city && cityCenters[city]) {
    map.setView(cityCenters[city], 11);
  }
}

// =============================================
// Theme
// =============================================
function applyTheme(theme) {
  document.documentElement.dataset.theme = theme;
  localStorage.setItem('theme', theme);
  themeLabel.textContent = theme === 'dark' ? 'Light' : 'Dark';
  if (currentState && currentState.years.length) {
    plotRates(currentState.years, currentState.baseRates, currentState.adjustedRates);
  }
}

// =============================================
// Contact form (API-backed)
// =============================================
if (contactDate) contactDate.min = new Date().toISOString().split('T')[0];

function prefillConsultationMessage() {
  if (!contactMessage) return;
  let details = [];
  if (currentState.totalPriceINR) {
    details.push(`Estimated Price: ${formatINR(currentState.totalPriceINR)}`);
    details.push(`Adjusted rate: ${formatINR(currentState.baseFutureRate * currentState.factor)}/sq ft`);
    details.push(`Size: ${currentState.size} sq ft`);
  }
  const city = citySel.value || '';
  const lm = landmarkSel.value || '';
  const cat = categorySel.value || '';
  if (city || lm || cat) details.push(`Selection: ${[lm, city].filter(Boolean).join(', ')} (${cat})`);
  contactMessage.value = (details.length ? details.join('\n') + '\n\n' : '') + (contactMessage.value || '');
}

if (contactForm) {
  contactForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    contactStatus.className = ''; contactStatus.style.display = 'none'; contactStatus.textContent = '';

    if (!contactConsent.checked) {
      contactStatus.className = 'error';
      contactStatus.textContent = 'Please agree to be contacted.';
      contactStatus.style.display = 'block';
      return;
    }

    const payload = {
      name: contactName.value.trim(),
      email: contactEmail.value.trim(),
      phone: contactPhone.value.trim(),
      purpose: contactPurpose.value,
      preferredDate: contactDate.value,
      preferredTime: contactTime.value,
      message: contactMessage.value.trim()
    };

    try {
      const result = await apiFetch(`${API_BASE}/contact`, {
        method: 'POST',
        body: JSON.stringify(payload)
      });

      contactStatus.className = 'success';
      contactStatus.textContent = result.message || 'Your consultation request has been submitted!';
      contactStatus.style.display = 'block';
      contactForm.reset();
    } catch (err) {
      // Fallback to mailto
      const mail = "jaashi117@gmail.com";
      const subject = `Consultation Request - ${payload.purpose || 'Inquiry'}`;
      const lines = [
        `Name: ${payload.name}`, `Email: ${payload.email}`, `Phone: ${payload.phone}`,
        `Purpose: ${payload.purpose}`, `Preferred: ${payload.preferredDate || ''} ${payload.preferredTime || ''}`.trim(), '', (payload.message || '')
      ];
      const body = encodeURIComponent(lines.join('\n'));
      window.location.href = `mailto:${mail}?subject=${encodeURIComponent(subject)}&body=${body}`;

      contactStatus.className = 'success';
      contactStatus.textContent = 'Opening your email app… If it did not open, please email us directly.';
      contactStatus.style.display = 'block';
    }
  });
}

// =============================================
// Events
// =============================================
themeToggle.addEventListener('click', () => {
  const next = isDark() ? 'light' : 'dark';
  applyTheme(next);
});

citySel.addEventListener('change', async () => {
  const city = citySel.value;
  fillDropdown(landmarkSel, [], 'Select Landmark');
  fillDropdown(categorySel, [], 'Select Category');
  landmarkSel.disabled = !city;
  categorySel.disabled = true;
  if (city) {
    try {
      await fetchLandmarks(city);
      landmarkSel.disabled = false;
    } catch (err) {
      showError(err.message);
    }
    updateMarkerForCity(city);
  }
  resultsCard.style.display = 'none';
  offersBody.innerHTML = "";
  offersNote.textContent = "Predict price to see personalized bank offers.";
  clearError();
});

landmarkSel.addEventListener('change', async () => {
  const city = citySel.value;
  const lm = landmarkSel.value;
  fillDropdown(categorySel, [], 'Select Category');
  categorySel.disabled = !lm;
  if (lm) {
    try {
      await fetchCategories(city, lm);
      categorySel.disabled = false;
    } catch (err) {
      showError(err.message);
    }
  }
  resultsCard.style.display = 'none';
  offersBody.innerHTML = "";
  offersNote.textContent = "Predict price to see personalized bank offers.";
  clearError();
});

predictionForm.addEventListener('submit', (e) => {
  e.preventDefault();
  runPrediction();
});

// Premium recalc — re-run prediction with updated premium inputs
function recalcPremiums() {
  if (resultsCard.style.display !== 'block') return;
  runPrediction();
}
roadWidthInput.addEventListener('input', recalcPremiums);
frontageInput.addEventListener('input', recalcPremiums);
cornerPlotSel.addEventListener('change', recalcPremiums);

// EMI/Offers recalc (stays client-side for instant feedback)
[interestInput, tenureInput, downPctInput].forEach(el => {
  el.addEventListener('input', () => {
    if (resultsCard.style.display !== 'block') return;
    updateEMIOutputs(currentState.totalPriceINR);
    renderBankOffers();
  });
});

// Offers filters
[offerTypeSel, offerCustomerSel, offerSortSel].forEach(el => el.addEventListener('change', renderBankOffers));

// Duty events
dutyUsePredSel.addEventListener('change', () => {
  const usePred = dutyUsePredSel.value === 'yes';
  dutyValueInput.disabled = usePred;
});
[dutyStateSel, dutyOwnerSel, dutyUsePredSel, dutyValueInput].forEach(el => {
  el.addEventListener('change', updateDutyUI);
  el.addEventListener('input', updateDutyUI);
});
dutyCalcBtn.addEventListener('click', updateDutyUI);

// =============================================
// Init
// =============================================
document.addEventListener('DOMContentLoaded', async () => {
  const saved = localStorage.getItem('theme') || 'light';
  applyTheme(saved);
  initMap();
  clearError();

  try {
    await fetchCities();
    await fetchDutyStates();
  } catch (err) {
    showError('Failed to connect to backend: ' + err.message);
  }

  renderBankOffers();
});