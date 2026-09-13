(() => {
  if (Project.name !== 'new_lambo') throw new Error('Wrong project');
  if (Cube.all.some(cube => cube.name === 'teardrop_crowned_hood')) throw new Error('Crowned hood already exists');
  const filesystem = require('fs');
  const backup = Project.save_path.replace(/\.bbmodel$/, '.before-crowned-hood.bbmodel');
  if (!filesystem.existsSync(backup)) filesystem.writeFileSync(backup, Codecs.project.compile());
  const existing = Cube.all.slice();
  const texture = Texture.all[0];
  const parent = Group.all.find(group => group.name === 'chassis');
  const cockpit = Group.all.find(group => group.name === 'cockpit');
  const removed = new Set(['sculpted_carbon_hood', 'continuous_hood_fender_bridge', 'smooth_windshield', 'slanted_a_pillar', 'slanted_side_glazing', 'front_fender_tip']);
  const created = [];
  Undo.initEdit({elements: existing, outliner: true});
  for (const cube of existing) if (removed.has(cube.name)) cube.remove();
  const panel = (name, corners, material, owner = parent, thickness = 0.8) => {
    const points = corners.map(point => new THREE.Vector3(...point));
    const center = points.reduce((sum, point) => sum.add(point), new THREE.Vector3()).multiplyScalar(0.25);
    const across = points[1].clone().sub(points[0]).add(points[3].clone().sub(points[2])).multiplyScalar(0.5);
    const along = points[2].clone().sub(points[0]).add(points[3].clone().sub(points[1])).multiplyScalar(0.5);
    const width = across.length();
    across.normalize();
    along.addScaledVector(across, -along.dot(across));
    const length = along.length();
    along.normalize();
    const normal = along.clone().cross(across).normalize();
    const rotation = new THREE.Euler().setFromRotationMatrix(new THREE.Matrix4().makeBasis(across, normal, along), 'ZYX');
    const dimensions = [width + 0.12, thickness, length + 0.12];
    const cube = new Cube({name, from: center.toArray().map((value, axis) => value - dimensions[axis] / 2), to: center.toArray().map((value, axis) => value + dimensions[axis] / 2), origin: center.toArray(), rotation: [rotation.x, rotation.y, rotation.z].map(value => value * 180 / Math.PI), box_uv: true, uv_offset: [material % 4 * 512 + 4, Math.floor(material / 4) * 384 + 4]}).addTo(owner).init();
    for (const face of Object.values(cube.faces)) face.texture = texture.uuid;
    created.push(cube);
    return cube;
  };
  const section = progress => ({
    depth: -89 + 61 * progress,
    width: 18 + 8 * Math.sin(progress * Math.PI / 2),
    center: 18 + 19 * (1 - (1 - progress) ** 2) + 3 * Math.sin(progress * Math.PI),
    crown: 2.5 * Math.sin(progress * Math.PI)
  });
  const point = (progress, across) => {
    const shape = section(progress);
    return [shape.width * across, shape.center - shape.crown * across ** 2, shape.depth];
  };
  const rows = 24;
  const columns = 12;
  for (let row = 0; row < rows; row++) {
    const start = row / rows;
    const end = (row + 1) / rows;
    for (let column = 0; column < columns; column++) {
      const left = -1 + column / columns * 2;
      const right = -1 + (column + 1) / columns * 2;
      panel('teardrop_crowned_hood', [point(start, left), point(start, right), point(end, left), point(end, right)], 4);
    }
    for (const side of [-1, 1]) {
      const outer = progress => {
        const shape = section(progress);
        const distance = Math.abs(shape.depth + 52);
        const arch = distance < 18.4 ? 17 + Math.sqrt(18.4 ** 2 - distance ** 2) + 0.5 : 18 + 17 * progress;
        return [side * 29.5, Math.min(shape.center - shape.crown, Math.max(arch, shape.center - shape.crown - 3.5)), shape.depth];
      };
      panel('convex_hood_red_transition', [point(start, side), outer(start), point(end, side), outer(end)], 0);
    }
  }
  panel('raised_smooth_windshield', [[-27, 37.2, -28], [27, 37.2, -28], [-23, 48, -5], [23, 48, -5]], 6, cockpit);
  for (const side of [-1, 1]) {
    panel('raised_a_pillar', [[side * 26.6, 37.2, -28], [side * 28, 37.2, -28], [side * 23.1, 48.4, -5], [side * 24.5, 48.4, -5]], 0, cockpit, 1.2);
    for (let band = 0; band < 16; band++) {
      const start = band / 16;
      const end = (band + 1) / 16;
      const edge = progress => {
        const height = 33 + 14.5 * progress;
        const front = height < 37.2 ? -28 : -28 + (height - 37.2) / 10.8 * 23;
        return {height, front, rear: 35 - progress * 12, width: 30 - progress * 7};
      };
      const lower = edge(start);
      const upper = edge(end);
      panel('matched_side_window', [[side * lower.width, lower.height, lower.front], [side * lower.width, lower.height, lower.rear], [side * upper.width, upper.height, upper.front], [side * upper.width, upper.height, upper.rear]], 6, cockpit, 0.6);
    }
    panel('nose_shoulder_blend', [[side * 29, 18, -88], [side * 32.5, 18, -88], [side * 29, 30, -70], [side * 37, 26, -70]], 0);
    panel('cowl_shoulder_closure', [[side * 26, 37, -28], [side * 29.5, 33.5, -28], [side * 26, 36, -25], [side * 30, 33, -25]], 0);
  }
  Canvas.updateAll();
  const centerAtAxle = section(37 / 61);
  const edgeAtAxle = centerAtAxle.center - centerAtAxle.crown;
  if (centerAtAxle.center <= 35.4 || centerAtAxle.center <= edgeAtAxle) throw new Error('Hood is still recessed');
  Undo.finishEdit('Crown hood and blend teardrop nose', {elements: Cube.all.slice(), outliner: true});
  filesystem.writeFileSync(Project.save_path, Codecs.project.compile());
  Project.saved = true;
  Preview.selected.camera.position.set(150, 95, -240);
  Preview.selected.controls.target.set(0, 24, 0);
  Preview.selected.controls.update();
  return JSON.stringify({saved: true,centerAtFrontAxle: centerAtAxle.center,edgeAtFrontAxle: edgeAtAxle,archHeight: 35.4,panelsAdded: created.length,totalCubes: Cube.all.length});
})()