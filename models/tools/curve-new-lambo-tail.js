(() => {
  if (Project.name !== 'new_lambo') throw new Error('Wrong project');
  if (Cube.all.some(cube => cube.name === 'rounded_fastback_surface')) throw new Error('Tail already curved');
  const filesystem = require('fs');
  const backup = Project.save_path.replace(/\.bbmodel$/, '.before-curved-tail.bbmodel');
  if (!filesystem.existsSync(backup)) filesystem.writeFileSync(backup, Codecs.project.compile());
  const texture = Texture.all[0];
  const before = Cube.all.slice();
  const groups = Object.fromEntries(Group.all.map(group => [group.name, group]));
  const removed = new Set(['smooth_carbon_roof_rear', 'swept_c_pillar', 'fastback_rear_glass', 'sloping_engine_deck', 'flush_engine_louver', 'rear_haunch_surface', 'rear_quarter_taper', 'window_roof_rail', 'swept_wing_support']);
  const created = [];
  Undo.initEdit({elements: before, outliner: true});
  for (const cube of before) if (removed.has(cube.name)) cube.remove();
  const panel = (name, corners, material, owner = groups.rear_engine, thickness = 0.9) => {
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
    const dimensions = [width + 0.14, thickness, length + 0.14];
    const cube = new Cube({name, from: center.toArray().map((value, axis) => value - dimensions[axis] / 2), to: center.toArray().map((value, axis) => value + dimensions[axis] / 2), origin: center.toArray(), rotation: [rotation.x, rotation.y, rotation.z].map(value => value * 180 / Math.PI), uv_offset: [material % 4 * 512 + 4, Math.floor(material / 4) * 384 + 4], box_uv: true}).addTo(owner).init();
    for (const face of Object.values(cube.faces)) face.texture = texture.uuid;
    created.push(cube);
    return cube;
  };
  const profile = depth => 28 + 21.4 * Math.cos((depth - 8) / 77 * Math.PI / 2);
  const width = depth => 23 + 3 * Math.sin((depth - 8) / 77 * Math.PI) - (depth - 8) / 77;
  const point = (depth, lateral) => [width(depth) * lateral, profile(depth) - 0.65 * lateral ** 2, depth];
  const outer = (depth, side) => {
    const progress = (depth - 8) / 77;
    const lateral = depth < 28 ? 24 + (depth - 8) * 0.55 : depth < 62 ? 35 + 4 * Math.sin((depth - 28) / 34 * Math.PI / 2) : 39 - (depth - 62) / 23 * 6;
    return [side * lateral, profile(depth) - 0.65 - 3.5 * Math.sin(progress * Math.PI), depth];
  };
  const sections = 40;
  for (let segment = 0; segment < sections; segment++) {
    const start = 8 + segment / sections * 77;
    const end = 8 + (segment + 1) / sections * 77;
    const midpoint = (start + end) / 2;
    for (let column = 0; column < 6; column++) {
      const left = -1 + column / 3;
      const right = -1 + (column + 1) / 3;
      panel('rounded_fastback_surface', [point(start, left), point(start, right), point(end, left), point(end, right)], midpoint < 23 || midpoint > 53 ? 4 : 6);
    }
    for (const side of [-1, 1]) {
      panel('rounded_rear_shoulder', [point(start, side), outer(start, side), point(end, side), outer(end, side)], midpoint < 23 ? 4 : 0);
      const first = outer(start, side);
      const last = outer(end, side);
      const lower = (depth, top) => {
        if (depth < 35) return Math.min(top - 0.2, 47.5 - (depth - 23) * 14.5 / 12);
        const distance = Math.abs(depth - 53);
        return distance < 18.8 ? 17 + Math.sqrt(18.8 ** 2 - distance ** 2) : 12;
      };
      const firstBottom = lower(start, first[1]);
      const lastBottom = lower(end, last[1]);
      if (start >= 23 && first[1] > firstBottom && last[1] > lastBottom) {
        panel('curved_rear_flank', [first, [first[0], firstBottom, start], last, [last[0], lastBottom, end]], 0, groups.chassis);
      }
    }
  }
  for (const side of [-1, 1]) {
    const mountStart = [side * 24, profile(65) + 0.7, 65];
    const mountEnd = [side * 24, 42.4, 70];
    panel('refitted_wing_support', [[mountStart[0] - 0.7, mountStart[1], mountStart[2]], [mountStart[0] + 0.7, mountStart[1], mountStart[2]], [mountEnd[0] - 0.7, mountEnd[1], mountEnd[2]], [mountEnd[0] + 0.7, mountEnd[1], mountEnd[2]]], 4, groups.aero, 1.3);
  }
  for (let index = 0; index < 7; index++) {
    const depth = 55 + index * 3.5;
    panel('curved_deck_vent', [[-17, profile(depth) + 0.25, depth], [17, profile(depth) + 0.25, depth], [-17, profile(depth + 0.8) + 0.25, depth + 0.8], [17, profile(depth + 0.8) + 0.25, depth + 0.8]], 9, groups.rear_engine, 0.3);
  }
  Canvas.updateAll();
  const heights = Array.from({length: 41}, (_, index) => profile(8 + index / 40 * 77));
  const slopes = heights.slice(1).map((height, index) => height - heights[index]);
  if (!heights.every((height, index) => index === 0 || height < heights[index - 1])) throw new Error('Tail must descend continuously');
  if (Math.abs(slopes[0]) >= Math.abs(slopes[slopes.length - 1])) throw new Error('Tail has no rounded slope progression');
  Undo.finishEdit('Curve roof into teardrop tail', {elements: Cube.all.slice(), outliner: true});
  filesystem.writeFileSync(Project.save_path, Codecs.project.compile());
  Project.saved = true;
  Preview.selected.camera.position.set(245, 56, 15);
  Preview.selected.controls.target.set(0, 24, 0);
  Preview.selected.controls.update();
  return JSON.stringify({saved: true,panelsAdded: created.length,roofHeight: heights[0],tailHeight: heights[40],initialDrop: slopes[0],finalDrop: slopes[slopes.length - 1],cubes: Cube.all.length});
})()