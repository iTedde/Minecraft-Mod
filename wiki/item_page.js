/* recipes + item pages */
const RECIPES = {{RECIPES}};
const REFS = {{REFS}};
const SOURCES = {{SOURCES}};
const MADE = {}, USED = {}, WS = {};
RECIPES.forEach((r, i) => {
  const add = (m, k) => { m[k] = m[k] || []; if (m[k][m[k].length - 1] !== i) m[k].push(i); };
  r.o.forEach(o => o.r.forEach(x => add(MADE, x)));
  const ins = [...(r.in || []), ...(r.g ? r.g.flat() : [])].filter(Boolean);
  ins.forEach(x => x.r.forEach(y => add(USED, y)));
  r.ws.forEach(w => add(WS, w));
});
function isCraft(r){ return r.startsWith('craft:') && BYID[r.slice(6)]; }
function refName(r){ if (isCraft(r)) return BYID[r.slice(6)].de; return REFS[r] ? REFS[r][0] : (r.split(':')[1] || r).replace(/_/g, ' '); }
function refIcon(r){ if (isCraft(r)) return BYID[r.slice(6)].i; return REFS[r] ? REFS[r][1] : ''; }
function refHref(r){ return '#/item/' + (r.startsWith('craft:') ? r.slice(6) : r); }
function refChip(r){ const ic = refIcon(r); return `<a class="chip" href="${refHref(r)}">${ic ? `<img src="${ic}" alt="">` : '<span class="noicon"></span>'}${esc(refName(r))}</a>`; }
function rslot(ing, big){
  if (!ing) return '<span class="slot"></span>';
  const alts = ing.r.length ? ing.r : ['unbekannt:leer'];
  const first = alts[0], ic = refIcon(first);
  let title = ing.t ? `Tag #${ing.t}: ` + alts.slice(0, 12).map(refName).join(', ') + (alts.length > 12 ? ' …' : '') : alts.map(refName).join(' / ');
  if (ing.n) title += ' (' + ing.n + ')';
  const cnt = ing.c ? `<span class="count">${ing.c}</span>` : '';
  const tagm = ing.t || alts.length > 1 ? '<span class="tagm">#</span>' : '';
  const data = alts.length > 1 ? ` data-alts="${esc(alts.join(' '))}"` : '';
  const img = ic ? `<img src="${ic}" alt="">` : `<span class="slotlabel">${esc(refName(first).slice(0, 6))}</span>`;
  return `<span class="slot${big ? ' big' : ''}" title="${esc(title)}"${data}><a href="${refHref(first)}">${img}${cnt}${tagm}</a></span>`;
}
function gridHtml(cells, cols){ return `<div class="gridn" style="grid-template-columns:repeat(${cols},38px)">${cells.join('')}</div>`; }
function renderRecipe(r){
  let left;
  if (r.g){
    const w = Math.max(1, ...r.g.map(x => x.length));
    const cells = [];
    r.g.forEach(row => { row.forEach(c => cells.push(rslot(c))); for (let k = row.length; k < w; k++) cells.push(rslot(null)); });
    left = gridHtml(cells, w);
  } else if (r.sl){
    const n = r.in.length, cols = n <= 1 ? 1 : n <= 4 ? 2 : 3, size = n <= 1 ? 1 : n <= 4 ? 4 : 9;
    const cells = r.in.map(c => rslot(c));
    while (cells.length < size) cells.push(rslot(null));
    left = gridHtml(cells, cols);
  } else {
    left = gridHtml(r.in.map(c => rslot(c)), Math.min(Math.max(r.in.length, 1), 3));
  }
  const one = r.o.length === 1;
  const outs = `<div class="gridn" style="grid-template-columns:repeat(${Math.min(Math.max(r.o.length, 1), 2)},${one ? 52 : 38}px)">${r.o.map(o => rslot(o, one)).join('')}</div>`;
  const arrow = r.k === 'smelting' ? '🔥' : '➜';
  const ws = r.ws.length > 1 ? ' ' + r.ws.map(w => { const ic = refIcon(w); return '<a class="wsicon" href="' + refHref(w) + '" title="' + esc(refName(w)) + '">' + (ic ? '<img src="' + ic + '" alt="">' : esc(refName(w))) + '</a>'; }).join('') : r.ws.length ? ' ' + refChip(r.ws[0]) : '';
  return `<div class="rcard"><div class="rhead"><b>${esc(r.l)}</b>${r.sl ? ' <small>(formlos)</small>' : ''}${ws}</div>` +
    `<div class="craftui">${left}<div class="arrow">${arrow}</div>${outs}</div>` +
    (r.m ? `<div class="rmeta">${esc(r.m)}</div>` : '') + `<div class="rid">${esc(r.id)}</div></div>`;
}
function recipeBlock(title, idx, hid, limit){
  if (!idx || !idx.length) return '';
  const first = idx.slice(0, limit).map(i => renderRecipe(RECIPES[i])).join('');
  const rest = idx.length > limit ? `<button class="hbtn more" data-more="rest-${hid}">Alle ${idx.length} anzeigen</button><div class="rlist" id="rest-${hid}" hidden>${idx.slice(limit).map(i => renderRecipe(RECIPES[i])).join('')}</div>` : '';
  return `<h2 id="item--${hid}">${title} <small>(${idx.length})</small></h2><div class="rlist">${first}</div>${rest}`;
}
function renderItem(key){
  const ref = key.includes(':') ? key : 'craft:' + key;
  const it = isCraft(ref) ? BYID[ref.slice(6)] : null;
  if (!it && !REFS[ref] && !MADE[ref] && !USED[ref] && !SOURCES[ref]) return false;
  const name = refName(ref), ic = refIcon(ref);
  const badge = it ? (it.b === 'new' ? '<span class="badge new">neu</span> ' : it.b === 'chg' ? '<span class="badge chg">geändert</span> ' : '') : '';
  const group = it ? it.g : (ref.startsWith('minecraft:') ? 'Minecraft (Vanilla)' : ref.split(':')[0]);
  const ment = it ? it.m.map(p => `<li><a href="#/${p}">${esc(TITLES[p] || p)}</a></li>`).join('') : '';
  const made = MADE[ref], used = USED[ref], ws = WS[ref];
  let h = `<div class="crumbs"><a href="#/items">Gegenstände & Blöcke</a> › ${esc(group)}</div><h1>${esc(name)}</h1>` +
    `<aside class="infobox"><div class="ibtitle">${esc(name)}</div><div class="ibimg">${ic ? `<img src="${ic}" alt="">` : '<span class="noicon" style="width:96px;height:96px"></span>'}</div><table>` +
    (it ? `<tr><th>Englisch</th><td>${esc(it.en)}</td></tr><tr><th>Typ</th><td>${it.k}</td></tr>` : '') +
    `<tr><th>Herkunft</th><td>${badge}${esc(group)}</td></tr><tr><th>ID</th><td><code>${esc(ref)}</code></td></tr>` +
    `<tr><th>Befehl</th><td><code>/give @s ${esc(ref)}</code></td></tr>` +
    `<tr><th>Rezepte</th><td>${(made || []).length}× Herstellung<br>${(used || []).length}× Verwendung${ws ? `<br>${ws.length}× in dieser Maschine` : ''}</td></tr></table></aside>`;
  if (it && it.p) h += `<p class="note tip"><b>Anleitung:</b> <a href="#/${it.p}">${esc(TITLES[it.p] || it.p)}</a></p>`;
  if (!it) h += '<p class="lead">Vanilla-Gegenstand. Hier stehen die Craft-Rezepte, in denen er vorkommt.</p>';
  h += '<p style="font-size:13px;color:var(--muted)">Zutaten anklicken führt zu deren Seite. Felder mit <b>#</b> akzeptieren mehrere Gegenstände (Tag) und wechseln ihr Bild durch; mit der Maus darüber siehst du alle.</p>';
  if (SOURCES[ref]) h += `<h2 id="item--source">Herkunft</h2><p class="lead">${esc(SOURCES[ref])}</p>`;
  h += recipeBlock('Herstellung', made, 'made', 24);
  if (!made && it && !SOURCES[ref]) h += '<h2 id="item--made">Herstellung</h2><p style="color:var(--muted)">Kein Rezept in den Datenpaketen. Im Spiel zeigt EMI mit <b>R</b> alle Wege; Erze und Rohstoffe: <a href="#/rohstoffe">Rohstoffe &amp; Erze</a>.</p>';
  h += recipeBlock('In dieser Maschine', ws, 'ws', 12);
  h += recipeBlock('Verwendung', used, 'used', 12);
  if (ment) h += `<h2 id="item--ment">Erwähnt in</h2><ul>${ment}</ul>`;
  h += '<div class="clear"></div>';
  const sec = $('#page-item');
  sec.innerHTML = h;
  sec.dataset.title = name;
  sec.querySelectorAll('[data-more]').forEach(b => b.onclick = () => { document.getElementById(b.dataset.more).hidden = false; b.remove(); });
  $$('section.page').forEach(s => s.hidden = s !== sec);
  $$('nav.side a').forEach(a => a.classList.toggle('active', a.dataset.nav === 'items'));
  document.title = name + ' – Craft-Wiki';
  window.scrollTo(0, 0);
  return true;
}
setInterval(() => {
  document.querySelectorAll('section.page:not([hidden]) .slot[data-alts]').forEach(s => {
    const alts = s.dataset.alts.split(' '), n = ((+s.dataset.i || 0) + 1) % alts.length;
    s.dataset.i = n;
    const a = s.querySelector('a'), img = a.querySelector('img'), ic = refIcon(alts[n]);
    a.href = refHref(alts[n]);
    if (img && ic) img.src = ic;
  });
}, 1200);

