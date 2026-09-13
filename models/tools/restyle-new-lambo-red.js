(async () => {
  if (Project.name !== 'new_lambo') throw new Error('Wrong project');
  if (Cube.all.some(cube => cube.name === 'red_reference_led_upper')) throw new Error('Reference already applied');
  const filesystem = require('fs');
  const projectPath = Project.save_path;
  const backup = projectPath.replace(/\.bbmodel$/, '.yellow-backup.bbmodel');
  if (!filesystem.existsSync(backup)) filesystem.writeFileSync(backup, Codecs.project.compile());
  const texture = Texture.all.find(item => item.name === 'new_lambo.png');
  if (!texture || texture.error) throw new Error('Texture unavailable');
  const previous = Cube.all.slice();
  const created = [];
  Undo.initEdit({elements: previous, outliner: true, textures: [texture], bitmap: true});
  const atlas = document.createElement('canvas');
  atlas.width = texture.width;
  atlas.height = texture.height;
  const context = atlas.getContext('2d');
  context.drawImage(texture.canvas, 0, 0);
  const palettes = {
    0: ['#b81720', '#bc1922', '#b3161e'],
    1: ['#e32c32', '#dc262c', '#e83136'],
    2: ['#87151d', '#921923', '#83131c'],
    4: ['#191c20', '#202327', '#1c1f22'],
    6: ['#151d23', '#182128', '#192229'],
    7: ['#333e46', '#37434b', '#354049'],
    8: ['#6f6556', '#7a7061', '#635c51'],
    9: ['#292a29', '#31312e', '#353630'],
    12: ['#191d20', '#22262a', '#1c2023'],
    14: ['#9e1a21', '#b31e26', '#c1252d']
  };
  let seed = 20260906;
  for (const [indexText, colors] of Object.entries(palettes)) {
    const index = Number(indexText);
    const offsetX = index % 4 * 512;
    const offsetY = Math.floor(index / 4) * 384;
    for (let row = 0; row < 384; row += 2) {
      for (let column = 0; column < 512; column += 2) {
        seed = (Math.imul(seed, 1664525) + 1013904223) >>> 0;
        context.fillStyle = colors[seed % colors.length];
        context.fillRect(offsetX + column, offsetY + row, 2, 2);
      }
    }
  }
  const offset = index => [index % 4 * 512 + 4, Math.floor(index / 4) * 384 + 4];
  const body = Group.all.find(group => group.name === 'chassis');
  const front = Group.all.find(group => group.name === 'front_fascia');
  const carbonNames = new Set(['hood_step', 'nose_center', 'yellow_roof', 'mirror_housing', 'cabin_shoulder_rail']);
  for (const cube of previous) {
    if (carbonNames.has(cube.name)) cube.uv_offset = offset(4);
    if (cube.name === 'headrest' || cube.name === 'sport_seat_back') cube.uv_offset = offset(12);
    if (cube.name === 'hub_badge') cube.uv_offset = offset(14);
    if (cube.name === 'yellow_roof') cube.name = 'carbon_roof';
    if (cube.name === 'windshield_glass') cube.uv_offset = offset(6);
    if (/^(led_|headlamp_|intake_yellow_lower_edge|side_intake|intake_rim)/.test(cube.name)) cube.remove();
  }
  const add = (name, from, to, material, parent = body, extra = {}) => {
    if (from.some((value, axis) => value >= to[axis])) throw new Error(`Invalid geometry: ${name}`);
    const cube = new Cube({name, from, to, uv_offset: offset(material), box_uv: true, ...extra}).addTo(parent).init();
    for (const face of Object.values(cube.faces)) face.texture = texture.uuid;
    created.push(cube);
    return cube;
  };
  const bar = (name, start, end, thickness, material, parent = front) => {
    const middle = start.map((value, axis) => (value + end[axis]) / 2);
    const length = Math.hypot(end[0] - start[0], end[1] - start[1]);
    return add(name, [middle[0] - length / 2, middle[1] - thickness / 2, middle[2] - thickness / 2], [middle[0] + length / 2, middle[1] + thickness / 2, middle[2] + thickness / 2], material, parent, {origin: middle, rotation: [0, 0, Math.atan2(end[1] - start[1], end[0] - start[0]) * 180 / Math.PI]});
  };
  for (let positionZ = -87; positionZ < -73; positionZ += 2) {
    const progress = (positionZ + 87) / 14;
    const half = 17 + progress * 7;
    add('carbon_nose_taper', [-half, 20 + progress, positionZ], [half, 22 + progress, positionZ + 2], 4, front);
  }
  for (const side of [-1, 1]) {
    const center = side * 28;
    add('angular_lamp_housing', [side < 0 ? -37 : 18, 11, -86.8], [side < 0 ? -18 : 37, 29, -84.5], 4, front);
    const junction = [side * 29.5, 21, -87.3];
    bar('red_reference_led_inner', [side * 18.5, 20, -87.3], junction, 0.85, 10);
    bar('red_reference_led_upper', junction, [side * 36, 28, -87.3], 0.85, 10);
    bar('red_reference_led_lower', junction, [side * 35, 13, -87.3], 0.85, 10);
    bar('red_front_blade_lower', [side * 17, 9, -87.5], [side * 36, 10.5, -87.5], 1.4, 0);
    bar('red_front_blade_outer', [side * 36, 10.5, -87.5], [side * 38, 22, -84], 1.4, 1);
    bar('champagne_splitter_edge', [side * 18, 8.1, -88], [side * 35, 9.1, -88], 0.45, 8);
    add('projector_one', [center - 1.8, 25, -87.1], [center - 0.8, 26.2, -86.7], 10, front);
    add('projector_two', [center + 0.8, 25, -87.1], [center + 1.8, 26.2, -86.7], 10, front);
    for (let height = 12; height < 34; height += 1) {
      const startZ = 17 + (height - 12) * 0.39;
      const endZ = 36 - (height - 12) * 0.08;
      add('deep_angular_side_intake', [side < 0 ? -39.65 : 39.05, height, startZ], [side < 0 ? -39.05 : 39.65, height + 1, endZ], 5);
      add('intake_carbon_leading_edge', [side < 0 ? -40.1 : 39.5, height, startZ - 1.5], [side < 0 ? -39.5 : 40.1, height + 1, startZ], 4);
    }
    add('red_rear_haunch', [side < 0 ? -37 : 31, 35.5, 32], [side < 0 ? -31 : 37, 37.5, 67], 0);
    add('seat_red_headrest_trim', [side * 13 - 4.2, 35, 9.7], [side * 13 + 4.2, 35.7, 10], 14, Group.all.find(group => group.name === 'cockpit'));
    add('side_carbon_sill_blade', [side < 0 ? -40.1 : 38.5, 7, -29], [side < 0 ? -38.5 : 40.1, 10, 28], 4);
  }
  for (const wheel of Group.all.filter(group => group.name.startsWith('wheel_'))) {
    const [wheelX, wheelY, wheelZ] = wheel.origin;
    const outside = Math.sign(wheelX) * 40.3;
    for (let spoke = 0; spoke < 10; spoke++) {
      for (const fork of [-1, 1]) {
        add('split_alloy_spoke', [outside - 0.85, wheelY + 5, wheelZ - 0.4], [outside + 0.85, wheelY + 12.3, wheelZ + 0.4], 8, wheel, {origin: [outside, wheelY, wheelZ], rotation: [spoke * 36 + fork * 7, 0, 0]});
      }
    }
  }
  texture.fromDataURL(atlas.toDataURL('image/png'));
  await new Promise(resolve => setTimeout(resolve, 200));
  texture.uv_width = 2048;
  texture.uv_height = 2048;
  Canvas.updateAll();
  Undo.finishEdit('Red and carbon reference supercar', {elements: Cube.all.slice(), outliner: true, textures: [texture], bitmap: true});
  texture.path = projectPath.replace(/\.bbmodel$/, '.png');
  filesystem.writeFileSync(texture.path, Buffer.from(atlas.toDataURL('image/png').split(',')[1], 'base64'));
  filesystem.writeFileSync(projectPath, Codecs.project.compile());
  Project.saved = true;
  Preview.selected.camera.position.set(180, 82, -235);
  Preview.selected.controls.target.set(0, 23, 0);
  Preview.selected.controls.update();
  return JSON.stringify({saved: true, cubes: Cube.all.length, added: created.length, roof: Cube.all.find(cube => cube.name === 'carbon_roof').to[1], textureError: texture.error, backup});
})()