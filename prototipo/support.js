/*
 * Motor liviano del prototipo — Tienda Virtual · Grupo 03 (SENA ADSO)
 * Ejecuta las pantallas .dc.html del mockup en cualquier navegador, sin cuentas ni servidores:
 * plantillas {{ }}, <sc-for>, <sc-if>, <dc-import>, eventos onClick / onChange y estado (setState).
 * Todos los datos de prueba (usuarios, productos, pedidos) están escritos dentro de cada pantalla.
 */
(function () {
  'use strict';

  var hide = document.createElement('style');
  hide.textContent = 'x-dc{display:none!important}';
  document.head.appendChild(hide);
  document.addEventListener('submit', function (e) { e.preventDefault(); }, true);

  var VOID = { area: 1, base: 1, br: 1, col: 1, embed: 1, hr: 1, img: 1, input: 1, link: 1, meta: 1, source: 1, track: 1, wbr: 1 };
  var SVG_NS = 'http://www.w3.org/2000/svg';
  var decoder = document.createElement('textarea');
  function decode(s) {
    if (s.indexOf('&') < 0) return s;
    decoder.innerHTML = s;
    return decoder.value;
  }

  /* ---------- Parser de la plantilla (respeta mayúsculas de atributos y etiquetas dentro de tablas) ---------- */
  function parse(src) {
    var root = { tag: '#root', attrs: [], children: [] };
    var stack = [root];
    var re = /<!--[\s\S]*?-->|<\/([a-zA-Z][\w:-]*)\s*>|<([a-zA-Z][\w:-]*)((?:\s+[^\s=>\/]+(?:\s*=\s*(?:"[^"]*"|'[^']*'|[^\s>]+))?)*)\s*(\/?)>/g;
    var last = 0, m;
    function text(t) { if (t) stack[stack.length - 1].children.push({ text: decode(t) }); }
    while ((m = re.exec(src))) {
      text(src.slice(last, m.index));
      last = re.lastIndex;
      if (m[0].slice(0, 4) === '<!--') continue;
      if (m[1]) {
        var close = m[1].toLowerCase();
        for (var k = stack.length - 1; k > 0; k--) {
          if (stack[k].tag.toLowerCase() === close) { stack.length = k; break; }
        }
        continue;
      }
      var node = { tag: m[2], attrs: parseAttrs(m[3] || ''), children: [] };
      stack[stack.length - 1].children.push(node);
      var low = node.tag.toLowerCase();
      if (low === 'style' || low === 'script') {
        var end = src.indexOf('</' + low, last);
        node.children.push({ text: src.slice(last, end), raw: true });
        re.lastIndex = last = src.indexOf('>', end) + 1;
        continue;
      }
      if (!VOID[low] && !m[4]) stack.push(node);
    }
    text(src.slice(last));
    return root.children;
  }
  function parseAttrs(s) {
    var out = [], re = /([^\s=>\/]+)(?:\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+)))?/g, m;
    while ((m = re.exec(s))) {
      var v = m[2] != null ? m[2] : (m[3] != null ? m[3] : (m[4] != null ? m[4] : ''));
      out.push([m[1], decode(v)]);
    }
    return out;
  }

  /* ---------- Componentes ---------- */
  function DCLogic(props) { this.props = props || {}; this.state = {}; }
  DCLogic.prototype.setState = function (patch) {
    if (typeof patch === 'function') patch = patch(this.state, this.props);
    this.state = Object.assign({}, this.state, patch || {});
    schedule();
  };
  DCLogic.prototype.forceUpdate = function () { schedule(); };
  DCLogic.prototype.renderVals = function () { return {}; };

  var defs = {};          // nombre -> { nodes, Klass }
  var instances = {};     // clave de posición -> instancia
  var mounted = [];

  function loadDef(name, src) {
    var start = src.indexOf('<x-dc>'), end = src.lastIndexOf('</x-dc>');
    var nodes = parse(src.slice(start + 6, end));
    var sm = /<script[^>]*data-dc-script[^>]*>([\s\S]*?)<\/script>/.exec(src);
    var Klass = sm ? new Function('DCLogic', sm[1] + '\n;return Component;')(DCLogic) : DCLogic;
    var helmet = [], body = [];
    nodes.forEach(function (n) { (n.tag && n.tag.toLowerCase() === 'helmet' ? helmet : body).push(n); });
    helmet.forEach(function (h) { h.children.forEach(addHead); });
    defs[name] = { nodes: body, Klass: Klass };
    var imports = [], im = /<dc-import[^>]*\sname="([^"]+)"/g, x;
    while ((x = im.exec(src))) if (imports.indexOf(x[1]) < 0) imports.push(x[1]);
    return imports;
  }
  var headSeen = {};
  function addHead(n) {
    if (!n.tag) return;
    var key = n.tag + JSON.stringify(n.attrs) + (n.children[0] ? n.children[0].text : '');
    if (headSeen[key]) return;
    headSeen[key] = 1;
    var el = document.createElement(n.tag);
    n.attrs.forEach(function (a) { el.setAttribute(a[0], a[1]); });
    if (n.children[0]) el.textContent = n.children[0].text;
    document.head.appendChild(el);
  }
  function fetchDef(name) {
    if (defs[name]) return Promise.resolve();
    return fetch(name + '.dc.html').then(function (r) { return r.text(); }).then(function (src) {
      return Promise.all(loadDef(name, src).map(fetchDef));
    });
  }

  /* ---------- Evaluación de {{ huecos }} ---------- */
  function lookup(path, scope) {
    path = path.trim();
    if (path === 'true') return true;
    if (path === 'false') return false;
    if (path === 'null') return null;
    if (/^-?\d+(\.\d+)?$/.test(path)) return Number(path);
    var parts = path.split('.'), v = scope;
    for (var i = 0; i < parts.length; i++) { if (v == null) return undefined; v = v[parts[i]]; }
    return v;
  }
  var HOLE = /\{\{([^}]*)\}\}/g;
  function single(v) { var m = /^\s*\{\{([^}]*)\}\}\s*$/.exec(v); return m ? m[1] : null; }
  function interp(s, scope) {
    return s.replace(HOLE, function (_, p) { var v = lookup(p, scope); return v == null ? '' : String(v); });
  }
  function attrVal(v, scope) { var p = single(v); return p != null ? lookup(p, scope) : interp(v, scope); }
  function camel(s) { return s.replace(/-([a-z])/g, function (_, c) { return c.toUpperCase(); }); }

  /* ---------- Render a nodos virtuales ---------- */
  function renderNodes(nodes, scope, ctx, out, svg) {
    for (var i = 0; i < nodes.length; i++) renderNode(nodes[i], scope, ctx, out, svg);
    return out;
  }
  function renderNode(n, scope, ctx, out, svg) {
    if (n.text != null) {
      if (n.raw) { out.push({ text: n.text }); return; }
      var t = interp(n.text, scope);
      if (t) out.push({ text: t });
      return;
    }
    var tag = n.tag, low = tag.toLowerCase(), a = {};
    n.attrs.forEach(function (x) { a[x[0]] = x[1]; });
    if (low === 'sc-if') {
      if (attrVal(a.value || '', scope)) renderNodes(n.children, scope, ctx, out, svg);
      return;
    }
    if (low === 'sc-for') {
      var list = attrVal(a.list || '', scope) || [], as = a.as || 'item';
      list.forEach(function (item, idx) {
        var s = Object.create(scope); s[as] = item; s.$index = idx;
        renderNodes(n.children, s, ctx, out, svg);
      });
      return;
    }
    if (low === 'dc-import') {
      var name = a.name, props = {};
      n.attrs.forEach(function (x) {
        if (x[0] === 'name' || x[0].indexOf('hint-') === 0) return;
        props[camel(x[0])] = attrVal(x[1], scope);
      });
      ctx.count[name] = (ctx.count[name] || 0) + 1;
      var key = ctx.key + '>' + name + '#' + ctx.count[name];
      renderComponent(name, props, key, out);
      return;
    }
    var isSvg = svg || low === 'svg';
    var v = { tag: isSvg ? tag : low, svg: isSvg, attrs: {}, props: {}, on: {}, children: [] };
    n.attrs.forEach(function (x) {
      var k = x[0], val = attrVal(x[1], scope);
      if (/^on[A-Z]/.test(k)) {
        if (typeof val !== 'function') return;
        var ev = k.slice(2).toLowerCase();
        if (ev === 'change' && !(low === 'select' || /^(checkbox|radio|file)$/i.test(a.type || ''))) ev = 'input';
        v.on[ev] = val;
        return;
      }
      if (k === 'value' && (low === 'input' || low === 'select' || low === 'textarea')) { v.props.value = val == null ? '' : String(val); return; }
      if (k === 'checked' && low === 'input') { v.props.checked = !!val; return; }
      if (k.indexOf('aria-') === 0) { v.attrs[k] = val == null ? '' : String(val); return; }
      if (val === false || val == null) return;
      v.attrs[k] = val === true ? '' : String(val);
    });
    renderNodes(n.children, scope, ctx, v.children, isSvg);
    out.push(v);
  }
  function renderComponent(name, props, key, out) {
    var def = defs[name];
    var inst = instances[key];
    if (!inst) {
      inst = instances[key] = new def.Klass(props);
      if (!inst.state) inst.state = {};
      mounted.push(inst);
    }
    inst.props = props;
    var vals = inst.renderVals() || {};
    renderNodes(def.nodes, vals, { key: key, count: {} }, out, false);
  }

  /* ---------- Aplicar nodos virtuales al DOM ---------- */
  function create(v) {
    if (v.text != null) return document.createTextNode(v.text);
    var el = v.svg ? document.createElementNS(SVG_NS, v.tag) : document.createElement(v.tag);
    update(el, v, true);
    return el;
  }
  function same(el, v) {
    if (v.text != null) return el.nodeType === 3;
    return el.nodeType === 1 && (v.svg ? el.tagName === v.tag : el.tagName.toLowerCase() === v.tag);
  }
  function update(el, v, fresh) {
    if (!fresh) {
      for (var i = el.attributes.length - 1; i >= 0; i--) {
        var nm = el.attributes[i].name;
        if (!(nm in v.attrs)) el.removeAttribute(nm);
      }
    }
    for (var k in v.attrs) if (el.getAttribute(k) !== v.attrs[k]) el.setAttribute(k, v.attrs[k]);
    el.__h = v.on;
    el.__b = el.__b || {};
    Object.keys(v.on).forEach(function (t) {
      if (el.__b[t]) return;
      el.__b[t] = 1;
      el.addEventListener(t, function (e) { var h = el.__h && el.__h[t]; if (h) h(e); });
    });
    patchChildren(el, v.children);
    if ('value' in v.props && el.value !== v.props.value) el.value = v.props.value;
    if ('checked' in v.props && el.checked !== v.props.checked) el.checked = v.props.checked;
  }
  function patchChildren(parent, vs) {
    var kids = parent.childNodes, i;
    for (i = 0; i < vs.length; i++) {
      var cur = kids[i], v = vs[i];
      if (!cur) parent.appendChild(create(v));
      else if (!same(cur, v)) parent.replaceChild(create(v), cur);
      else if (v.text != null) { if (cur.nodeValue !== v.text) cur.nodeValue = v.text; }
      else update(cur, v, false);
    }
    while (kids.length > vs.length) parent.removeChild(kids[kids.length - 1]);
  }

  /* ---------- Ciclo de render ---------- */
  var rootEl, pending = false, pageName;
  function render() {
    pending = false;
    var before = mounted.length;
    var out = [];
    renderComponent(pageName, {}, 'root', out);
    patchChildren(rootEl, out);
    mounted.slice(before).forEach(function (inst) { if (inst.componentDidMount) inst.componentDidMount(); });
  }
  function schedule() {
    if (pending || !rootEl) return;
    pending = true;
    (window.queueMicrotask || setTimeout)(render);
  }

  function boot() {
    pageName = decodeURIComponent(location.pathname.split('/').pop() || 'Main.dc.html').replace(/\.dc\.html$/, '');
    // La copia que el navegador ya interpretó de <x-dc> se quita para no duplicar ids ni etiquetas.
    Array.prototype.forEach.call(document.querySelectorAll('x-dc'), function (n) { n.remove(); });
    rootEl = document.createElement('div');
    rootEl.id = 'dc-root';
    document.body.appendChild(rootEl);
    var idx = document.createElement('a');
    idx.href = 'index.html';
    idx.textContent = '☰ Índice de pantallas';
    idx.setAttribute('style', 'position:fixed;left:12px;bottom:12px;z-index:99;background:#0F172A;color:#fff;padding:10px 14px;border-radius:999px;font:600 13px Inter,system-ui,sans-serif;text-decoration:none;box-shadow:0 6px 18px rgba(15,23,42,.3)');
    document.body.appendChild(idx);
    fetch(location.pathname).then(function (r) { return r.text(); }).then(function (src) {
      return Promise.all(loadDef(pageName, src).map(fetchDef));
    }).then(render).catch(function (err) {
      rootEl.innerHTML = '<p style="font-family:sans-serif;padding:24px">No se pudo cargar la pantalla. Ábrela desde GitHub Pages o un servidor web (no como archivo local).</p>';
      console.error(err);
    });
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', boot);
  else boot();
})();
