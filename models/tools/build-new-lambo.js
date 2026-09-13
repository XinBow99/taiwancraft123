(async () => {
  if (Project.name !== 'new_lambo' || Outliner.elements.length) throw new Error('Expected empty new_lambo');
  const filesystem = require('fs');
  const path = Project.save_path;
  if (!path) throw new Error('Save path required');
  const backup = path.replace(/\.bbmodel$/, '.before-build.bbmodel');
  if (!filesystem.existsSync(backup)) filesystem.writeFileSync(backup, Codecs.project.compile());
  const elements = [];
  Undo.initEdit({elements, outliner: true, textures: [], uv_mode: true});
  Project.texture_width = 2048;
  Project.texture_height = 2048;
  Project.box_uv = true;
  const palette = {
    yellow: ['#f8bf16', '#efb20c', '#ffcd2c'],
    highlight: ['#ffd33a', '#f9c72a', '#ffdc4f'],
    shade: ['#c68c0e', '#d79d12', '#e2a617'],
    rubber: ['#191b1c', '#202223', '#252728'],
    carbon: ['#20272a', '#272d2f', '#303739'],
    grille: ['#101719', '#22292b', '#151c1e'],
    glass: ['#263d48', '#29414d', '#2d4652'],
    glassLight: ['#536976', '#586f7c', '#607681'],
    silver: ['#7e8588', '#969c9d', '#646e73'],
    rim: ['#383f42', '#434b4f', '#50575b'],
    white: ['#f7fcff', '#eaf6ff', '#ffffff'],
    red: ['#dd2820', '#f63b24', '#b91e19'],
    seat: ['#c99416', '#daab26', '#b78010'],
    interior: ['#20262a', '#282e31', '#181e22'],
    brake: ['#e7ad13', '#ffd12a', '#c6940f'],
    exhaust: ['#6c757b', '#838d91', '#545e64']
  };
  const names = Object.keys(palette);
  const atlas = document.createElement('canvas');
  atlas.width = 2048;
  atlas.height = 2048;
  const context = atlas.getContext('2d');
  let seed = 2026;
  for (let index = 0; index < names.length; index++) {
    const offsetX = (index % 4) * 512;
    const offsetY = Math.floor(index / 4) * 384;
    const colors = palette[names[index]];
    for (let pixelY = 0; pixelY < 384; pixelY += 2) {
      for (let pixelX = 0; pixelX < 512; pixelX += 2) {
        seed = (Math.imul(seed, 1664525) + 1013904223) >>> 0;
        context.fillStyle = colors[seed % colors.length];
        context.fillRect(offsetX + pixelX, offsetY + pixelY, 2, 2);
      }
    }
  }
  const root = new Group({name: 'new_lambo', origin: [0, 0, 0]}).init();
  const group = (name, origin = [0, 0, 0]) => new Group({name, origin}).addTo(root).init();
  const body = group('chassis');
  const front = group('front_fascia');
  const cabin = group('cockpit');
  const rear = group('rear_engine');
  const aero = group('aero');
  const cube = (name, from, to, material, parent = body, extra = {}) => {
    const index = names.indexOf(material);
    if (index < 0 || from.some((value, axis) => value >= to[axis])) throw new Error(`Invalid cube ${name}`);
    const element = new Cube({name, from, to, box_uv: true, uv_offset: [(index % 4) * 512 + 4, Math.floor(index / 4) * 384 + 4], ...extra}).addTo(parent).init();
    elements.push(element);
    return element;
  };
  const bar = (name, start, end, thickness, material, parent) => {
    const middle = start.map((value, axis) => (value + end[axis]) / 2);
    const length = Math.hypot(end[0] - start[0], end[1] - start[1]);
    return cube(name, [middle[0] - length / 2, middle[1] - thickness / 2, middle[2] - thickness / 2], [middle[0] + length / 2, middle[1] + thickness / 2, middle[2] + thickness / 2], material, parent, {origin: middle, rotation: [0, 0, Math.atan2(end[1] - start[1], end[0] - start[0]) * 180 / Math.PI]});
  };
  cube('undertray', [-32, 5, -80], [32, 8, 81], 'carbon');
  cube('central_floor', [-28, 8, -65], [28, 15, 73], 'interior');
  cube('front_splitter', [-36, 5, -87], [36, 8, -69], 'carbon', aero);
  cube('front_splitter_tip', [-27, 5, -89], [27, 7, -83], 'rubber', aero);
  cube('rear_diffuser_base', [-36, 6, 72], [36, 10, 87], 'carbon', aero);
  for (let positionZ = -84; positionZ < -20; positionZ += 2) {
    const progress = (positionZ + 84) / 64;
    const top = 20 + progress * 13;
    const half = 24 - progress * 3;
    cube('hood_step', [-half, top - 3, positionZ], [half, top, positionZ + 2], 'yellow');
    for (const side of [-1, 1]) {
      const minX = side < 0 ? -half - 3 : half;
      cube('hood_crease', [minX, top - 1, positionZ], [minX + 3, top + 0.6, positionZ + 2], 'highlight');
    }
  }
  for (const side of [-1, 1]) {
    const door = group(side < 0 ? 'right_door' : 'left_door', [side * 31, 25, -24]);
    for (let positionZ = -82; positionZ < 84; positionZ += 2) {
      const wheelDistance = Math.min(Math.abs(positionZ + 52 + 1), Math.abs(positionZ - 53 + 1));
      const arch = wheelDistance < 18 ? 17 + Math.sqrt(18 * 18 - wheelDistance * wheelDistance) : 8;
      const top = positionZ < -60 ? 24 + (positionZ + 82) * 0.45 : positionZ < -24 ? 35.5 : positionZ < 27 ? 30 : 36;
      const outer = positionZ < -70 ? 35 + (positionZ + 82) * 0.18 : 39;
      if (top > arch + 0.3) {
        cube('body_side_panel', [side < 0 ? -outer : 29, arch, positionZ], [side < 0 ? -29 : outer, top, positionZ + 2], positionZ > -25 && positionZ < 25 ? 'yellow' : 'highlight', positionZ > -25 && positionZ < 25 ? door : body);
      }
      if (wheelDistance < 18 && top > arch) {
        const edgeX = side < 0 ? -outer - 0.1 : outer - 1;
        cube('wheel_arch_lip', [edgeX, arch - 0.4, positionZ], [edgeX + 1.1, Math.min(top, arch + 1.3), positionZ + 2], 'shade');
      }
    }
    cube('side_sill', [side < 0 ? -39 : 32, 6, -32], [side < 0 ? -32 : 39, 10, 32], 'carbon', aero);
    cube('yellow_sill_rail', [side < 0 ? -39.5 : 37, 10, -30], [side < 0 ? -37 : 39.5, 12, 31], 'highlight', aero);
    for (let height = 13; height < 31; height += 2) {
      const positionZ = 21 + (height - 13) * 0.35;
      cube('side_intake', [side < 0 ? -39.2 : 38.2, height, positionZ], [side < 0 ? -38.2 : 39.2, height + 2, positionZ + 10], 'grille');
      cube('intake_rim', [side < 0 ? -40 : 38.4, height, positionZ - 1.5], [side < 0 ? -38.4 : 40, height + 2, positionZ], 'shade');
    }
    cube('door_handle', [side < 0 ? -39.4 : 39, 27, 9], [side < 0 ? -39 : 39.4, 28, 16], 'carbon', door);
    cube('mirror_stalk', [side < 0 ? -43 : 31, 32, -22], [side < 0 ? -31 : 43, 34, -19], 'carbon', door);
    cube('mirror_housing', [side < 0 ? -47 : 39, 33, -25], [side < 0 ? -39 : 47, 37, -18], 'yellow', door);
    cube('mirror_glass', [side < 0 ? -46 : 40, 34, -17.9], [side < 0 ? -40 : 46, 36, -17.5], 'silver', door);
  }
  cube('nose_black_grille', [-33, 9, -83], [33, 20, -78], 'grille', front);
  cube('nose_center', [-19, 16, -87], [19, 22, -80], 'yellow', front);
  for (const side of [-1, 1]) {
    const centerX = side * 27;
    cube('front_intake_recess', [centerX - 8, 9, -85], [centerX + 8, 18, -82], 'grille', front);
    for (let height = 10; height < 18; height += 2) {
      for (let offset = -6; offset < 7; offset += 3) cube('intake_mesh', [centerX + offset, height, -85.2], [centerX + offset + 1, height + 0.7, -84.8], 'carbon', front);
    }
    bar('intake_yellow_lower_edge', [centerX - 8, 9, -85.5], [centerX + 7, 9, -85.5], 1.8, 'highlight', front);
    const minX = side < 0 ? -36 : 20;
    cube('headlamp_black_recess', [minX, 20, -80], [minX + 16, 25, -72], 'carbon', front);
    const junction = [side * 29, 22, -80.6];
    bar('led_inner', [side * 19, 20.5, -80.6], junction, 1.15, 'white', front);
    bar('led_upper', junction, [side * 35, 27.5, -80.6], 1.15, 'white', front);
    bar('led_lower', junction, [side * 33, 17, -80.6], 1.15, 'white', front);
    for (let step = 0; step < 8; step++) {
      cube('lamp_fender_slope', [side < 0 ? -37 : 34, 22 + step, -76 + step * 2], [side < 0 ? -34 : 37, 24 + step, -74 + step * 2], 'yellow', front);
    }
    cube('hood_vent', [side < 0 ? -24 : 20, 30, -42], [side < 0 ? -20 : 24, 31, -30], 'carbon', front);
  }
  cube('nose_badge', [-1, 23, -75], [1, 23.6, -72], 'shade', front);
  cube('dashboard', [-27, 27, -24], [27, 33, -14], 'interior', cabin);
  cube('rear_cabin_wall', [-27, 22, 26], [27, 37, 30], 'interior', cabin);
  for (const side of [-1, 1]) {
    const seatX = side * 13;
    cube('seat_cushion', [seatX - 7, 16, -6], [seatX + 7, 20, 10], 'seat', cabin);
    cube('sport_seat_back', [seatX - 7, 20, 8], [seatX + 7, 36, 13], 'seat', cabin, {origin: [seatX, 20, 10], rotation: [-10, 0, 0]});
    cube('headrest', [seatX - 4, 35, 10], [seatX + 4, 41, 14], 'seat', cabin);
  }
  cube('center_console', [-3, 17, -8], [3, 24, 16], 'carbon', cabin);
  cube('steering_hub', [-16, 28, -14], [-10, 32, -10], 'carbon', cabin);
  for (let band = 0; band < 15; band++) {
    const positionY = 33 + band;
    const positionZ = -29 + band * 1.6;
    const half = 28 - band * 0.22;
    cube('windshield_glass', [-half, positionY, positionZ], [half, positionY + 1, positionZ + 1.9], band > 8 && band < 11 ? 'glassLight' : 'glass', cabin);
    for (const side of [-1, 1]) {
      cube('windshield_pillar', [side < 0 ? -half - 2 : half, positionY, positionZ], [side < 0 ? -half : half + 2, positionY + 1.3, positionZ + 2.2], 'highlight', cabin);
    }
  }
  cube('roof_black_gasket', [-26, 46, -6], [26, 48, 24], 'carbon', cabin);
  cube('yellow_roof', [-25, 48, -5], [25, 50, 24], 'highlight', cabin);
  for (const side of [-1, 1]) {
    for (let band = 0; band < 15; band++) {
      const height = 33 + band;
      const outer = 31 - band * 0.32;
      const startZ = -25 + band * 1.45;
      const endZ = 35 - band * 0.7;
      cube('side_window', [side < 0 ? -outer : outer - 1, height, startZ], [side < 0 ? -outer + 1 : outer, height + 1, endZ], 'glass', cabin);
      cube('rear_pillar', [side < 0 ? -outer - 1 : outer - 1, height, endZ], [side < 0 ? -outer + 1 : outer + 1, height + 1.2, endZ + 4], 'yellow', cabin);
    }
    cube('window_sill', [side < 0 ? -33 : 30, 31, -27], [side < 0 ? -30 : 33, 33, 35], 'highlight', cabin);
  }
  for (let band = 0; band < 12; band++) {
    cube('rear_window', [-23, 47 - band, 24 + band * 1.8], [23, 48 - band, 26 + band * 1.8], 'glass', rear);
  }
  cube('engine_deck', [-28, 31, 44], [28, 34, 78], 'yellow', rear);
  for (let positionZ = 45; positionZ < 73; positionZ += 4) {
    cube('engine_cover_louver', [-22, 34, positionZ], [22, 35.5, positionZ + 2], 'carbon', rear);
  }
  cube('rear_bumper', [-36, 16, 78], [36, 31, 85], 'yellow', rear);
  cube('rear_vent', [-32, 16, 84], [32, 25, 85.5], 'grille', rear);
  for (const side of [-1, 1]) {
    cube('tail_light_housing', [side < 0 ? -35 : 17, 27, 84], [side < 0 ? -17 : 35, 31, 85.6], 'carbon', rear);
    cube('tail_light_red', [side < 0 ? -34 : 18, 28, 85.6], [side < 0 ? -18 : 34, 29.3, 86], 'red', rear);
    cube('wing_support', [side * 24 - 1.5, 34, 63], [side * 24 + 1.5, 43, 69], 'carbon', aero);
    cube('wing_endplate', [side < 0 ? -41 : 39, 43, 62], [side < 0 ? -39 : 41, 49, 74], 'carbon', aero);
    cube('exhaust_outer', [side * 9 - 4, 11, 82], [side * 9 + 4, 16, 88], 'exhaust', rear);
    cube('exhaust_dark_center', [side * 9 - 3, 12, 88], [side * 9 + 3, 15, 88.4], 'grille', rear);
  }
  cube('rear_wing', [-40, 43, 62], [40, 46, 74], 'carbon', aero);
  for (let positionX = -28; positionX <= 28; positionX += 8) {
    cube('diffuser_fin', [positionX, 4, 76], [positionX + 1.5, 11, 88], 'carbon', aero);
  }
  for (const side of [-1, 1]) {
    for (const wheelZ of [-52, 53]) {
      const wheelX = side * 35;
      const wheel = group(`wheel_${side < 0 ? 'right' : 'left'}_${wheelZ < 0 ? 'front' : 'rear'}`, [wheelX, 17, wheelZ]);
      const outsideX = side * 40.3;
      for (let segment = 0; segment < 24; segment++) {
        const angle = segment * 15;
        cube('tire_segment', [wheelX - 5, 30.5, wheelZ - 2.5], [wheelX + 5, 34, wheelZ + 2.5], 'rubber', wheel, {origin: [wheelX, 17, wheelZ], rotation: [angle, 0, 0]});
        cube('alloy_outer_ring', [outsideX - 0.5, 28.7, wheelZ - 1.8], [outsideX + 0.5, 30.1, wheelZ + 1.8], 'silver', wheel, {origin: [outsideX, 17, wheelZ], rotation: [angle, 0, 0]});
      }
      cube('rim_shadow', [wheelX - 4.8, 7.5, wheelZ - 9.5], [wheelX + 4.8, 26.5, wheelZ + 9.5], 'grille', wheel);
      cube('brake_caliper', [outsideX - 0.8, 13, wheelZ + 6], [outsideX + 0.8, 22, wheelZ + 10], 'brake', wheel);
      for (let spoke = 0; spoke < 10; spoke++) {
        cube('alloy_spoke', [outsideX - 0.7, 17, wheelZ - 0.85], [outsideX + 0.7, 29.2, wheelZ + 0.85], 'rim', wheel, {origin: [outsideX, 17, wheelZ], rotation: [spoke * 36, 0, 0]});
      }
      cube('hub', [outsideX - 1, 14.5, wheelZ - 2.5], [outsideX + 1, 19.5, wheelZ + 2.5], 'carbon', wheel);
      cube('hub_badge', [outsideX - 1.2, 16, wheelZ - 1], [outsideX + 1.2, 18, wheelZ + 1], 'shade', wheel);
    }
  }
  const texture = new Texture({name: 'new_lambo.png'}).fromDataURL(atlas.toDataURL('image/png')).add(false);
  texture.uv_width = 2048;
  texture.uv_height = 2048;
  for (const element of elements) for (const face of Object.values(element.faces)) face.texture = texture.uuid;
  await new Promise(resolve => setTimeout(resolve, 250));
  Canvas.updateAll();
  Undo.finishEdit('Build oversized yellow supercar', {elements, outliner: true, textures: [texture], uv_mode: true});
  const texturePath = path.replace(/\.bbmodel$/, '.png');
  texture.path = texturePath;
  filesystem.writeFileSync(texturePath, Buffer.from(atlas.toDataURL('image/png').split(',')[1], 'base64'));
  filesystem.writeFileSync(path, Codecs.project.compile());
  Project.saved = true;
  Preview.selected.setProjectionMode(false);
  Preview.selected.camera.position.set(180, 105, -230);
  Preview.selected.controls.target.set(0, 22, 0);
  Preview.selected.controls.update();
  return JSON.stringify({name: Project.name, cubes: elements.length, groups: Group.all.length, roofHeight: 50, characterHeight: 35, textureError: texture.error, saved: Project.saved});
})()