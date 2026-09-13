(() => {
  if (Project.name !== 'new_lambo') throw new Error('Wrong project');
  if (Cube.all.some(cube => cube.name === 'projector_lens_center')) throw new Error('Headlights already detailed');
  const filesystem = require('fs');
  const backup = Project.save_path.replace(/\.bbmodel$/, '.before-headlight-detail.bbmodel');
  if (!filesystem.existsSync(backup)) filesystem.writeFileSync(backup, Codecs.project.compile());
  Timeline.pause();
  Timeline.setTime(0);
  if (Animation.selected) Animator.preview();
  const texture = Texture.all[0];
  const owner = Group.all.find(group => group.name === 'front_fascia');
  const existing = Cube.all.slice();
  const created = [];
  const removeNames = new Set(['swept_led_inner', 'swept_led_upper', 'swept_led_lower', 'raked_headlight_recess', 'nose_shoulder_blend']);
  Undo.initEdit({elements: existing, outliner: true});
  for (const cube of existing) {
    if (removeNames.has(cube.name)) cube.remove();
    if (cube.name === 'front_corner_taper') {
      cube.from[2] += 3;
      cube.to[2] += 3;
      cube.origin[2] += 3;
    }
  }
  const normal = new THREE.Vector3(0, 0.65, -1).normalize();
  const vertical = new THREE.Vector3(0, 1, 0.65).normalize();
  const horizontal = new THREE.Vector3(1, 0, 0);
  const basis = new THREE.Matrix4().makeBasis(horizontal, normal, vertical);
  const planeRotation = new THREE.Euler().setFromRotationMatrix(basis, 'ZYX');
  const localPoint = (side, positionX, positionY, depth = 0) => new THREE.Vector3(side * positionX, positionY, -93 + (positionY - 12) * 0.65).addScaledVector(normal, depth);
  const box = (name, center, dimensions, material, rotation = planeRotation) => {
    const cube = new Cube({name, from: center.toArray().map((value, axis) => value - dimensions[axis] / 2), to: center.toArray().map((value, axis) => value + dimensions[axis] / 2), origin: center.toArray(), rotation: [rotation.x, rotation.y, rotation.z].map(value => value * 180 / Math.PI), uv_offset: [material % 4 * 512 + 4, Math.floor(material / 4) * 384 + 4], box_uv: true}).addTo(owner).init();
    for (const face of Object.values(cube.faces)) face.texture = texture.uuid;
    created.push(cube);
    return cube;
  };
  const segment = (name, side, start, end, width, material, depth) => {
    const first = localPoint(side, start[0], start[1], depth);
    const last = localPoint(side, end[0], end[1], depth);
    const direction = last.clone().sub(first);
    const across = direction.clone().normalize();
    const along = across.clone().cross(normal).normalize();
    const rotation = new THREE.Euler().setFromRotationMatrix(new THREE.Matrix4().makeBasis(across, normal, along), 'ZYX');
    return box(name, first.add(last).multiplyScalar(0.5), [direction.length() + width * 0.25, 0.18, width], material, rotation);
  };
  const vertices = [[18, 20.8], [37.5, 30], [35, 12.5]];
  const spans = [];
  for (let row = 0; row < 50; row++) {
    const height = 12.5 + (row + 0.5) / 50 * 17.5;
    const left = height <= 20.8 ? 35 + (18 - 35) * (height - 12.5) / 8.3 : 18 + (37.5 - 18) * (height - 20.8) / 9.2;
    const right = 35 + 2.5 * (height - 12.5) / 17.5;
    spans.push({height,left,right});
  }
  for (const side of [-1, 1]) {
    for (const span of spans) {
      const midpoint = (span.left + span.right) / 2;
      box('triangular_headlamp_shell', localPoint(side, midpoint, span.height, -0.2), [span.right - span.left + 0.07, 1.15, 17.5 / 50 * Math.sqrt(1 + 0.65 ** 2) + 0.06], 4);
      if (span.right - span.left > 0.7) box('recessed_optical_bed', localPoint(side, midpoint, span.height, 0.4), [span.right - span.left - 0.35, 0.12, 17.5 / 50 * Math.sqrt(1 + 0.65 ** 2) + 0.02], span.height > 21 ? 6 : 5);
    }
    for (let edge = 0; edge < 3; edge++) {
      const start = vertices[edge];
      const end = vertices[(edge + 1) % 3];
      segment('headlamp_red_surround', side, start, end, 0.7, 0, 0.35);
      segment('headlamp_satin_bezel', side, start, end, 0.22, 9, 0.61);
    }
    const junction = [29.9, 21.1];
    for (const end of [[19.5, 20.9], [36.2, 28.7], [34.3, 14.3]]) {
      segment('led_dark_channel', side, junction, end, 0.9, 9, 0.57);
      segment('flush_y_led', side, junction, end, 0.38, 10, 0.72);
    }
    box('led_junction', localPoint(side, junction[0], junction[1], 0.74), [0.5, 0.19, 0.5], 10);
    for (const [positionX,positionY] of [[29.8,25.8],[32.8,27]]) {
      for (let arc = 0; arc < 12; arc++) {
        const angle = arc / 12 * Math.PI * 2;
        const next = (arc + 1) / 12 * Math.PI * 2;
        segment('projector_ring', side, [positionX + 0.8 * Math.cos(angle),positionY + 0.8 / Math.sqrt(1.4225) * Math.sin(angle)], [positionX + 0.8 * Math.cos(next),positionY + 0.8 / Math.sqrt(1.4225) * Math.sin(next)], 0.24, 9, 0.65);
      }
      for (let row = -3; row <= 3; row++) {
        const radius = 0.51;
        const offset = row * 0.14;
        const half = Math.sqrt(Math.max(0,radius * radius - offset * offset));
        box('projector_lens_center', localPoint(side, positionX, positionY + offset / Math.sqrt(1.4225), 0.76), [half * 2,0.15,0.15], 10);
      }
    }
    segment('lower_recess_fin', side, [25.5,18.4], [32.2,17.1], 0.28, 9, 0.58);
    segment('upper_reflector_edge', side, [25.2,23.7], [28.6,24.6], 0.18, 8, 0.56);
    segment('outer_red_cheek', side, [37.5,30], [38,17], 1.4, 0, -0.25);
  }
  Canvas.updateAll();
  Undo.finishEdit('Rebuild recessed projector headlights and flush Y lights', {elements: Cube.all.slice(), outliner: true});
  const lights = created.filter(cube => cube.name === 'flush_y_led');
  if (lights.length !== 6 || created.filter(cube => cube.name === 'projector_lens_center').length !== 28) throw new Error('Lamp completeness failed');
  filesystem.writeFileSync(Project.save_path, Codecs.project.compile());
  Project.saved = true;
  Preview.selected.camera.position.set(100, 72, -180);
  Preview.selected.controls.target.set(0, 23, -40);
  Preview.selected.controls.update();
  return JSON.stringify({saved:true,added:created.length,ledBranches:lights.length,projectors:4,animations:Animation.all.length});
})()