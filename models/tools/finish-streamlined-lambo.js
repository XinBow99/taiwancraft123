(() => {
  if (Project.name !== 'new_lambo') throw new Error('Wrong project');
  const existing = Cube.all.slice();
  const texture = Texture.all[0];
  const parent = Group.all.find(group => group.name === 'chassis');
  const created = [];
  Undo.initEdit({elements: existing, outliner: true});
  for (const cube of existing) {
    if (cube.name === 'sloping_red_front_shoulder') cube.remove();
    if (cube.name.startsWith('swept_led_')) {
      cube.from[1] += 1.8;
      cube.to[1] += 1.8;
      cube.origin[1] += 1.8;
      cube.from[2] -= 1.8;
      cube.to[2] -= 1.8;
      cube.origin[2] -= 1.8;
    }
  }
  const panel = (name, vertices, material) => {
    const points = vertices.map(point => new THREE.Vector3(...point));
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
    const dimensions = [width + 0.35, 1.1, length + 0.35];
    const element = new Cube({name, from: center.toArray().map((value, axis) => value - dimensions[axis] / 2), to: center.toArray().map((value, axis) => value + dimensions[axis] / 2), origin: center.toArray(), rotation: [rotation.x, rotation.y, rotation.z].map(value => value * 180 / Math.PI), box_uv: true, uv_offset: [material % 4 * 512 + 4, Math.floor(material / 4) * 384 + 4]}).addTo(parent).init();
    for (const face of Object.values(element.faces)) face.texture = texture.uuid;
    created.push(element);
  };
  const section = depth => {
    const progress = (depth + 89) / 61;
    const hoodHeight = 17 + 15 * progress - 1.8 * Math.sin(progress * Math.PI);
    const distance = Math.abs(depth + 52);
    const archHeight = distance < 18.4 ? 17 + Math.sqrt(18.4 * 18.4 - distance * distance) : hoodHeight + 1;
    return {depth, inner: 18 + 8 * progress, height: hoodHeight, outerHeight: Math.max(hoodHeight + 1, archHeight + 0.3)};
  };
  for (let index = 0; index < 32; index++) {
    const first = section(-89 + index / 32 * 61);
    const last = section(-89 + (index + 1) / 32 * 61);
    for (const side of [-1, 1]) {
      panel('continuous_hood_fender_bridge', [[side * first.inner, first.height, first.depth], [side * 29.5, first.outerHeight, first.depth], [side * last.inner, last.height, last.depth], [side * 29.5, last.outerHeight, last.depth]], 0);
    }
  }
  for (const side of [-1, 1]) {
    panel('front_fender_tip', [[side * 29, 18, -88], [side * 34, 18, -88], [side * 29, 25, -70], [side * 37, 25, -70]], 0);
  }
  Canvas.updateAll();
  Undo.finishEdit('Join hood to wheel arches and expose running lights', {elements: Cube.all.slice(), outliner: true});
  require('fs').writeFileSync(Project.save_path, Codecs.project.compile());
  Project.saved = true;
  return JSON.stringify({saved: true,added: created.length,cubes: Cube.all.length});
})()