(() => {
  if (Project.name !== 'new_lambo' || !Format.animation_mode) throw new Error('Wrong project or animation unsupported');
  if (Animation.all.some(animation => animation.name.startsWith('animation.new_lambo.'))) throw new Error('Car animations already exist');
  const filesystem = require('fs');
  const backup = Project.save_path.replace(/\.bbmodel$/, '.before-animations.bbmodel');
  if (!filesystem.existsSync(backup)) filesystem.writeFileSync(backup, Codecs.project.compile());
  const before = Cube.all.map(cube => ({uuid: cube.uuid, from: [...cube.from], to: [...cube.to], rotation: [...cube.rotation]}));
  const root = Group.all.find(group => group.name === 'new_lambo');
  const groups = Object.fromEntries(Group.all.map(group => [group.name, group]));
  Undo.initEdit({elements: Cube.all.slice(), outliner: true});
  const coachwork = new Group({name: 'suspension_body', origin: [0, 17, 0]}).addTo(root).init();
  for (const group of root.children.slice()) {
    if (group instanceof Group && group !== coachwork && !group.name.startsWith('wheel_')) group.addTo(coachwork);
  }
  for (const side of ['right', 'left']) {
    const sign = side === 'right' ? -1 : 1;
    const door = groups[`${side}_door`];
    door.origin = [sign * 31, 32, -28];
    for (const cube of Cube.all) {
      if (['matched_side_window', 'mirror_arm', 'aero_mirror'].includes(cube.name)) {
        const centerX = (cube.from[0] + cube.to[0]) / 2;
        if (Math.sign(centerX) === sign) cube.addTo(door);
      }
    }
    const wheel = groups[`wheel_${side}_front`];
    const steering = new Group({name: `steering_${side}`, origin: [...wheel.origin]}).addTo(root).init();
    wheel.addTo(steering);
    groups[steering.name] = steering;
    for (const axle of ['front', 'rear']) {
      const wheelGroup = groups[`wheel_${side}_${axle}`];
      const caliper = wheelGroup.children.find(cube => cube.name === 'brake_caliper');
      if (caliper) caliper.addTo(axle === 'front' ? steering : root);
    }
  }
  Canvas.updateAll();
  const unchanged = before.every(state => {
    const cube = Cube.all.find(item => item.uuid === state.uuid);
    return cube && ['from', 'to', 'rotation'].every(key => cube[key].every((value, axis) => value === state[key][axis]));
  });
  if (!unchanged) throw new Error('Rest geometry changed');
  Undo.finishEdit('Rig scissor doors, steering, suspension and fixed calipers', {elements: Cube.all.slice(), outliner: true});
  const created = [];
  Undo.initEdit({animations: created});
  const animation = (name, length, loop) => {
    const item = new Animation({name: `animation.new_lambo.${name}`, length, loop, snapping: 20}).add();
    created.push(item);
    return item;
  };
  const track = (item, bone, channel, frames) => {
    const animator = item.getBoneAnimator(bone);
    for (const [time, values] of frames) {
      animator.addKeyframe({channel, time, interpolation: 'linear', data_points: [{x: String(values[0]), y: String(values[1]), z: String(values[2])}]});
    }
  };
  const idle = animation('idle', 1.6, 'loop');
  track(idle, coachwork, 'position', [[0, [0, 0, 0]], [0.4, [0, 0.045, 0]], [0.8, [0, 0, 0]], [1.2, [0, -0.035, 0]], [1.6, [0, 0, 0]]]);
  track(idle, coachwork, 'rotation', [[0, [0, 0, 0]], [0.4, [0, 0, 0.06]], [0.8, [0, 0, 0]], [1.2, [0, 0, -0.06]], [1.6, [0, 0, 0]]]);
  const drive = animation('drive', 1.2, 'loop');
  for (const wheel of Group.all.filter(group => group.name.startsWith('wheel_'))) {
    track(drive, wheel, 'rotation', [[0, [0, 0, 0]], [0.3, [-90, 0, 0]], [0.6, [-180, 0, 0]], [0.9, [-270, 0, 0]], [1.2, [-360, 0, 0]]]);
  }
  track(drive, coachwork, 'position', [[0, [0, 0, 0]], [0.3, [0, 0.09, 0]], [0.6, [0, 0, 0]], [0.9, [0, 0.09, 0]], [1.2, [0, 0, 0]]]);
  for (const [name, angle] of [['steer_left', -24], ['steer_right', 24]]) {
    const steering = animation(name, 0.8, 'hold');
    for (const side of ['right', 'left']) {
      track(steering, groups[`steering_${side}`], 'rotation', [[0, [0, 0, 0]], [0.2, [0, angle * 0.15, 0]], [0.6, [0, angle * 0.85, 0]], [0.8, [0, angle, 0]]]);
    }
  }
  const open = animation('doors_open', 1.8, 'hold');
  const close = animation('doors_close', 1.8, 'once');
  const showcase = animation('showcase', 8, 'loop');
  for (const side of ['right', 'left']) {
    const sign = side === 'right' ? -1 : 1;
    const door = groups[`${side}_door`];
    const rotation = fraction => [-64 * fraction, sign * 12 * fraction, sign * 4 * fraction];
    track(open, door, 'rotation', [[0, rotation(0)], [0.3, rotation(0.08)], [0.9, rotation(0.5)], [1.5, rotation(0.92)], [1.8, rotation(1)]]);
    track(close, door, 'rotation', [[0, rotation(1)], [0.3, rotation(0.92)], [0.9, rotation(0.5)], [1.5, rotation(0.08)], [1.8, rotation(0)]]);
    track(showcase, door, 'rotation', [[0, rotation(0)], [0.6, rotation(0)], [1, rotation(0.08)], [1.8, rotation(0.7)], [2.4, rotation(1)], [4.2, rotation(1)], [4.8, rotation(0.7)], [5.6, rotation(0.08)], [6, rotation(0)], [8, rotation(0)]]);
    track(showcase, groups[`steering_${side}`], 'rotation', [[0, [0, 0, 0]], [1, [0, -18, 0]], [3, [0, -18, 0]], [5, [0, 18, 0]], [6.5, [0, 18, 0]], [8, [0, 0, 0]]]);
  }
  Undo.finishEdit('Add idle, drive, steering and scissor-door animations', {animations: created});
  Modes.options.animate.select();
  showcase.select();
  Timeline.setTime(3);
  Animator.preview();
  filesystem.writeFileSync(Project.save_path, Codecs.project.compile());
  Project.saved = true;
  Preview.selected.camera.position.set(175, 120, -255);
  Preview.selected.controls.target.set(0, 36, 0);
  Preview.selected.controls.update();
  return JSON.stringify({saved: true,restGeometryUnchanged: unchanged,animations: created.map(item => ({name: item.name,length: item.length,loop: item.loop})),doorParts: ['right','left'].map(side=>({side,count:groups[`${side}_door`].children.length})),cubes: Cube.all.length});
})()