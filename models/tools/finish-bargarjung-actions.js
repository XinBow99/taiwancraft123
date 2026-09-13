(() => {
  if (Project.name !== 'bargarjung') throw new Error('Wrong project');
  const smoke = Animation.all.find(animation => animation.name.endsWith('.smoke'));
  const walk = Animation.all.find(animation => animation.name.endsWith('.walk'));
  const arm = Group.all.find(group => group.name === 'right_arm');
  const forearm = Group.all.find(group => group.name === 'right_forearm');
  const prop = Group.all.find(group => group.name === 'cigarette');
  const head = Group.all.find(group => group.name === 'head');
  smoke.select();
  Timeline.setTime(2.8);
  Animator.preview();
  scene.updateMatrixWorld(true);
  const mouth = head.mesh.localToWorld(new THREE.Vector3(0, 2, -3.5));
  const target = mouth.clone().add(new THREE.Vector3(-0.15, -0.15, -0.7));
  const radians = value => value * Math.PI / 180;
  const handPosition = angles => new THREE.Vector3(0, -5.5, -2)
    .applyAxisAngle(new THREE.Vector3(1, 0, 0), radians(angles[3]))
    .add(new THREE.Vector3(-2, -5, 0.5))
    .applyEuler(new THREE.Euler(radians(angles[0]), radians(angles[1]), radians(angles[2]), 'ZYX'))
    .add(new THREE.Vector3(-5, 24, 0));
  let angles = [50, -70, 30, 110];
  const limits = [[0, 110], [-110, 30], [-70, 70], [60, 150]];
  const score = values => handPosition(values).distanceToSquared(target);
  for (const step of [15, 5, 1, 0.2, 0.04]) {
    for (let iteration = 0; iteration < 150; iteration++) {
      let changed = false;
      for (let axis = 0; axis < angles.length; axis++) {
        for (const direction of [-1, 1]) {
          const candidate = angles.slice();
          candidate[axis] += step * direction;
          if (candidate[axis] < limits[axis][0] || candidate[axis] > limits[axis][1]) continue;
          if (score(candidate) < score(angles)) {
            angles = candidate;
            changed = true;
          }
        }
      }
      if (!changed) break;
    }
  }
  if (Math.sqrt(score(angles)) > 0.15) throw new Error('Smoking hand placement failed');
  Undo.initEdit({animations: Animation.all.slice()});
  const setRotation = (group, rotation) => {
    for (const frame of smoke.animators[group.uuid].rotation) {
      if (Math.abs(frame.time - 2.2) < 0.01 || Math.abs(frame.time - 3.6) < 0.01) {
        frame.data_points[0].extend({x: rotation[0], y: rotation[1], z: rotation[2]});
      }
    }
  };
  setRotation(arm, [angles[0], angles[1], angles[2]]);
  setRotation(forearm, [angles[3], 0, 0]);
  Animator.preview();
  scene.updateMatrixWorld(true);
  const inverse = forearm.mesh.getWorldQuaternion(new THREE.Quaternion()).invert();
  const wristRotation = new THREE.Euler().setFromQuaternion(inverse, 'ZYX');
  const rotation = [wristRotation.x * 180 / Math.PI, wristRotation.y * 180 / Math.PI, wristRotation.z * 180 / Math.PI];
  const animator = smoke.animators[prop.uuid];
  for (const frame of animator.rotation.slice()) frame.remove();
  for (const [time, values] of [[0, [0, 0, 0]], [1, [0, 0, 0]], [2.2, rotation], [3.6, rotation], [4.8, [0, 0, 0]], [8, [0, 0, 0]]]) {
    animator.addKeyframe({channel: 'rotation', time, data_points: [{x: values[0], y: values[1], z: values[2]}], interpolation: 'linear'});
  }
  const root = Group.all.find(group => group.name === 'bargarjung');
  for (const frame of walk.animators[root.uuid].position) {
    frame.data_points[0].y = Math.abs(frame.time % 0.6) < 0.01 ? '-1.1' : '0';
  }
  for (const animation of Animation.all) {
    for (const frame of animation.animators[prop.uuid].scale) {
      const value = animation === smoke ? '1' : '0';
      frame.data_points[0].extend({x: value, y: value, z: value});
    }
  }
  Undo.finishEdit('Align smoking hand and walking height');
  for (const animation of Animation.all) {
    animation.select();
    Timeline.setTime(animation.length / 2);
    Animator.preview();
    const expected = animation === smoke ? 1 : 0;
    if (prop.mesh.scale.toArray().some(value => Math.abs(value - expected) > 0.0001)) {
      throw new Error(`Prop visibility failed: ${animation.name}`);
    }
  }
  smoke.select();
  Timeline.setTime(2.8);
  Animator.preview();
  scene.updateMatrixWorld(true);
  const actual = prop.mesh.getWorldPosition(new THREE.Vector3());
  const error = actual.distanceTo(target);
  if (error > 0.2) throw new Error(`Rendered hand position mismatch: ${error}`);
  const filesystem = require('fs');
  const texture = Texture.all.find(item => item.name === 'bargarjung.png');
  filesystem.writeFileSync(texture.path, Buffer.from(texture.canvas.toDataURL('image/png').split(',')[1], 'base64'));
  filesystem.writeFileSync(Project.save_path, Codecs.project.compile());
  Project.saved = true;
  return JSON.stringify({handError: error, angles, animations: Animation.all.map(animation => ({name: animation.name, length: animation.length, loop: animation.loop})), saved: Project.saved});
})()