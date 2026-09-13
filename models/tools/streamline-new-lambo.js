(() => {
  if (Project.name !== 'new_lambo' || !Format.rotate_cubes) throw new Error('Wrong project or unsupported format');
  if (Cube.all.some(cube => cube.name === 'sculpted_carbon_hood')) throw new Error('Streamlined shell already exists');
  const filesystem = require('fs');
  const backup = Project.save_path.replace(/\.bbmodel$/, '.before-streamline.bbmodel');
  if (!filesystem.existsSync(backup)) filesystem.writeFileSync(backup, Codecs.project.compile());
  const texture = Texture.all.find(item => item.name === 'new_lambo.png');
  const existing = Cube.all.slice();
  const wheelState = existing.filter(cube => cube.parent.name.startsWith('wheel_')).map(cube => ({uuid: cube.uuid, from: [...cube.from], to: [...cube.to]}));
  const removeNames = new Set([
    'hood_step', 'hood_crease', 'hood_shoulder_fill', 'hood_vent', 'body_side_panel', 'wheel_arch_lip',
    'lamp_fender_slope', 'cabin_shoulder_rail', 'red_rear_haunch', 'windshield_glass', 'windshield_pillar',
    'side_window', 'rear_pillar', 'window_sill', 'rear_window', 'rear_bumper', 'nose_center',
    'carbon_nose_taper', 'angular_lamp_housing', 'nose_black_grille', 'front_intake_recess', 'intake_mesh',
    'red_reference_led_inner', 'red_reference_led_upper', 'red_reference_led_lower', 'red_front_blade_lower',
    'red_front_blade_outer', 'champagne_splitter_edge', 'projector_one', 'projector_two', 'nose_badge',
    'deep_angular_side_intake', 'intake_carbon_leading_edge', 'side_sill', 'yellow_sill_rail',
    'rear_wing', 'wing_support', 'wing_endplate', 'front_splitter', 'front_splitter_tip', 'roof_black_gasket',
    'carbon_roof', 'engine_deck', 'engine_cover_louver', 'mirror_housing', 'mirror_glass', 'mirror_stalk'
  ]);
  const created = [];
  Undo.initEdit({elements: existing, outliner: true});
  for (const cube of existing) if (removeNames.has(cube.name)) cube.remove();
  const groups = Object.fromEntries(Group.all.map(group => [group.name, group]));
  const materialOffset = material => [material % 4 * 512 + 4, Math.floor(material / 4) * 384 + 4];
  const box = (name, center, dimensions, material, parent = groups.chassis, rotation = [0, 0, 0]) => {
    const element = new Cube({name, from: center.map((value, axis) => value - dimensions[axis] / 2), to: center.map((value, axis) => value + dimensions[axis] / 2), origin: center, rotation, box_uv: true, uv_offset: materialOffset(material)}).addTo(parent).init();
    for (const face of Object.values(element.faces)) face.texture = texture.uuid;
    created.push(element);
    return element;
  };
  const vector = values => new THREE.Vector3(...values);
  const degrees = radians => radians * 180 / Math.PI;
  const panel = (name, corners, material, thickness = 1.2, parent = groups.chassis) => {
    const vertices = corners.map(vector);
    const center = vertices.reduce((sum, point) => sum.add(point), new THREE.Vector3()).multiplyScalar(0.25);
    const across = vertices[1].clone().sub(vertices[0]).add(vertices[3].clone().sub(vertices[2])).multiplyScalar(0.5);
    const along = vertices[2].clone().sub(vertices[0]).add(vertices[3].clone().sub(vertices[1])).multiplyScalar(0.5);
    const width = across.length();
    const axisAcross = across.normalize();
    along.addScaledVector(axisAcross, -along.dot(axisAcross));
    const length = along.length();
    const axisAlong = along.normalize();
    const normal = axisAlong.clone().cross(axisAcross).normalize();
    const basis = new THREE.Matrix4().makeBasis(axisAcross, normal, axisAlong);
    const angles = new THREE.Euler().setFromRotationMatrix(basis, 'ZYX');
    return box(name, center.toArray(), [width + 0.25, thickness, length + 0.25], material, parent, [degrees(angles.x), degrees(angles.y), degrees(angles.z)]);
  };
  const beam = (name, start, end, thickness, material, parent = groups.front_fascia) => {
    const direction = vector(end).sub(vector(start));
    const quaternion = new THREE.Quaternion().setFromUnitVectors(new THREE.Vector3(1, 0, 0), direction.clone().normalize());
    const angles = new THREE.Euler().setFromQuaternion(quaternion, 'ZYX');
    return box(name, start.map((value, axis) => (value + end[axis]) / 2), [direction.length(), thickness, thickness], material, parent, [degrees(angles.x), degrees(angles.y), degrees(angles.z)]);
  };
  for (let segment = 0; segment < 8; segment++) {
    const start = segment / 8;
    const end = (segment + 1) / 8;
    const section = progress => ({width: 18 + 8 * progress, height: 17 + 15 * progress - 1.8 * Math.sin(progress * Math.PI), depth: -89 + 61 * progress});
    const first = section(start);
    const last = section(end);
    panel('sculpted_carbon_hood', [[-first.width, first.height, first.depth], [first.width, first.height, first.depth], [-last.width, last.height, last.depth], [last.width, last.height, last.depth]], 4, 1.4);
    for (const side of [-1, 1]) {
      const outerFirst = 30 + 8 * Math.sin(start * Math.PI / 2);
      const outerLast = 30 + 8 * Math.sin(end * Math.PI / 2);
      panel('sloping_red_front_shoulder', [[side * first.width, first.height + 0.2, first.depth], [side * outerFirst, first.height + 3, first.depth], [side * last.width, last.height + 0.2, last.depth], [side * outerLast, last.height + 3, last.depth]], 0, 1.3);
    }
  }
  for (const side of [-1, 1]) {
    for (const wheelZ of [-52, 53]) {
      for (let segment = 0; segment < 16; segment++) {
        const start = segment / 16 * Math.PI;
        const end = (segment + 1) / 16 * Math.PI;
        const firstY = 17 + 18.4 * Math.sin(start);
        const firstZ = wheelZ + 18.4 * Math.cos(start);
        const lastY = 17 + 18.4 * Math.sin(end);
        const lastZ = wheelZ + 18.4 * Math.cos(end);
        panel('continuous_fender_arch', [[side * 29, firstY, firstZ], [side * 39, firstY, firstZ], [side * 29, lastY, lastZ], [side * 39, lastY, lastZ]], 0, 1.6);
        panel('dark_arch_trim', [[side * 39, firstY - 0.8, firstZ], [side * 40, firstY - 0.8, firstZ], [side * 39, lastY - 0.8, lastZ], [side * 40, lastY - 0.8, lastZ]], 4, 0.8);
      }
    }
    const door = side < 0 ? groups.right_door : groups.left_door;
    panel('door_upper_sculpture_front', [[side * 38, 33, -32], [side * 34.5, 21, -32], [side * 35, 31, 0], [side * 32.5, 19, 0]], 0, 1.8, door);
    panel('door_upper_sculpture_rear', [[side * 35, 31, 0], [side * 32.5, 19, 0], [side * 39, 35, 29], [side * 35, 21, 29]], 0, 1.8, door);
    panel('door_lower_sculpture_front', [[side * 34.5, 21, -32], [side * 36, 10, -32], [side * 32.5, 19, 0], [side * 35, 10, 0]], 2, 1.5, door);
    panel('door_lower_sculpture_rear', [[side * 32.5, 19, 0], [side * 35, 10, 0], [side * 35, 21, 29], [side * 38, 12, 29]], 0, 1.5, door);
    beam('rising_sill_line', [side * 37, 9, -32], [side * 39, 12, 32], 1.6, 4, groups.aero);
    panel('swept_side_intake', [[side * 38.2, 33, 23], [side * 38.8, 33, 33], [side * 35.8, 14, 14], [side * 38.8, 14, 28]], 5, 1.0);
    beam('intake_diagonal_blade', [side * 38.5, 34, 23], [side * 36, 14, 14], 1.7, 4);
    panel('rear_quarter_taper', [[side * 39, 34, 68], [side * 37, 11, 68], [side * 33, 27, 85], [side * 31, 10, 85]], 0, 1.6);
    panel('front_corner_taper', [[side * 37, 27, -71], [side * 36, 9, -71], [side * 32, 19, -88], [side * 30, 8, -88]], 0, 1.5);
    panel('rear_haunch_surface', [[side * 25, 33, 31], [side * 38, 35, 31], [side * 24, 29, 83], [side * 34, 29, 83]], 0, 1.5);
  }
  panel('smooth_windshield', [[-27, 32.3, -29], [27, 32.3, -29], [-23, 48, -5], [23, 48, -5]], 6, 0.8, groups.cockpit);
  panel('smooth_carbon_roof_front', [[-23, 48.5, -6], [23, 48.5, -6], [-23, 49.4, 8], [23, 49.4, 8]], 4, 1.1, groups.cockpit);
  panel('smooth_carbon_roof_rear', [[-23, 49.4, 8], [23, 49.4, 8], [-21.5, 47.5, 23], [21.5, 47.5, 23]], 4, 1.1, groups.cockpit);
  for (const side of [-1, 1]) {
    beam('slanted_a_pillar', [side * 28, 32, -29], [side * 24, 48.5, -5], 1.7, 0, groups.cockpit);
    beam('window_roof_rail', [side * 24, 48.5, -5], [side * 22.5, 47.5, 23], 1.4, 4, groups.cockpit);
    beam('swept_c_pillar', [side * 22.5, 47.5, 23], [side * 31, 33, 39], 3, 0, groups.cockpit);
    for (let band = 0; band < 16; band++) {
      const lower = band / 16;
      const upper = (band + 1) / 16;
      const section = progress => [side * (30 - progress * 7), 33 + progress * 14.5, -25 + progress * 21, 35 - progress * 12];
      const first = section(lower);
      const last = section(upper);
      panel('slanted_side_glazing', [[first[0], first[1], first[2]], [first[0], first[1], first[3]], [last[0], last[1], last[2]], [last[0], last[1], last[3]]], 6, 0.6, groups.cockpit);
    }
    beam('mirror_arm', [side * 31, 32, -21], [side * 41, 33, -24], 1.2, 4);
    box('aero_mirror', [side * 43, 34, -23], [7, 2.4, 5], 4, groups.chassis, [0, side * -15, 0]);
  }
  panel('fastback_rear_glass', [[-21.5, 47, 23], [21.5, 47, 23], [-25, 33, 47], [25, 33, 47]], 6, 0.8, groups.rear_engine);
  panel('sloping_engine_deck', [[-25, 33, 46], [25, 33, 46], [-24, 29, 81], [24, 29, 81]], 4, 1.2, groups.rear_engine);
  for (let louver = 0; louver < 7; louver++) {
    const depth = 49 + louver * 4;
    box('flush_engine_louver', [0, 33 - (depth - 46) * 4 / 35, depth], [39, 0.7, 1.3], 9, groups.rear_engine, [6.5, 0, 0]);
  }
  for (const side of [-1, 1]) {
    panel('raked_headlight_recess', [[side * 18, 17, -88], [side * 34, 17, -88], [side * 24, 29, -75], [side * 38, 29, -75]], 4, 1.5, groups.front_fascia);
    const junction = [side * 30, 20, -85.5];
    beam('swept_led_inner', [side * 18.5, 18, -87.4], junction, 0.85, 10);
    beam('swept_led_upper', junction, [side * 36.5, 28, -76], 0.85, 10);
    beam('swept_led_lower', junction, [side * 35, 11, -89], 0.85, 10);
    panel('lower_front_intake', [[side * 18, 8, -87.5], [side * 34, 8, -87.5], [side * 20, 16.5, -87], [side * 32, 16.5, -85]], 5, 1.0, groups.front_fascia);
    beam('diagonal_front_blade', [side * 17, 8, -89], [side * 35, 11, -89], 1.3, 0);
    beam('splitter_corner', [side * 35, 7.5, -88], [side * 38, 8, -73], 1.8, 4, groups.aero);
  }
  box('thin_front_splitter', [0, 7, -87], [67, 1.5, 6], 4, groups.aero);
  panel('center_lower_nose', [[-17, 8, -88], [17, 8, -88], [-18, 16.5, -88.5], [18, 16.5, -88.5]], 5, 1, groups.front_fascia);
  panel('rear_sloping_bumper', [[-32, 11, 85], [32, 11, 85], [-35, 28, 81], [35, 28, 81]], 0, 1.5, groups.rear_engine);
  box('thin_carbon_wing', [0, 43, 68], [79, 1.5, 9], 4, groups.aero, [-5, 0, 0]);
  for (const side of [-1, 1]) {
    beam('swept_wing_support', [side * 24, 32, 66], [side * 24, 42, 70], 1.5, 4, groups.aero);
    box('wing_end_fin', [side * 39, 44, 68], [1, 4, 10], 4, groups.aero, [-5, 0, 0]);
  }
  Canvas.updateAll();
  const unchanged = wheelState.every(state => {const cube = Cube.all.find(item => item.uuid === state.uuid);return cube && cube.from.every((value, axis) => value === state.from[axis]) && cube.to.every((value, axis) => value === state.to[axis]);});
  if (!unchanged) throw new Error('Wheel geometry changed');
  Undo.finishEdit('Reshape streamlined shell with inclined panels', {elements: Cube.all.slice(), outliner: true});
  filesystem.writeFileSync(Project.save_path, Codecs.project.compile());
  Project.saved = true;
  Preview.selected.camera.position.set(175, 82, -230);
  Preview.selected.controls.target.set(0, 23, 0);
  Preview.selected.controls.update();
  return JSON.stringify({saved: true, addedPanels: created.length, totalCubes: Cube.all.length, wheelGeometryUnchanged: unchanged, meshes: Mesh.all.length});
})()