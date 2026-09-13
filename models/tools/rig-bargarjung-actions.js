(() => {
  if (Project.name !== 'bargarjung') throw new Error('Wrong project');
  if (Group.all.some(group => group.name === 'right_forearm')) throw new Error('Action rig already exists');
  const filesystem = require('fs');
  const backup = Project.save_path.replace(/\.bbmodel$/, '.before-actions.bbmodel');
  if (!filesystem.existsSync(backup)) filesystem.writeFileSync(backup, Codecs.project.compile());
  const before = Cube.all.map(cube => ({uuid: cube.uuid, from: [...cube.from], to: [...cube.to]}));
  const texture = Texture.all.find(item => item.name === 'bargarjung.png');
  const created = [];
  Undo.initEdit({elements: Cube.all.slice(), outliner: true, textures: [texture], bitmap: true});
  for (const [side, suffix, positionX] of [['right', '-1', -7], ['left', '1', 7]]) {
    const arm = Group.all.find(group => group.name === `${side}_arm`);
    const forearm = new Group({name: `${side}_forearm`, origin: [positionX, 19, 0.5]}).addTo(arm).init();
    for (const prefix of ['tattoo_forearm_', 'hand_', 'thumb_']) {
      Cube.all.find(cube => cube.name === prefix + suffix).addTo(forearm);
    }
  }
  const forearm = Group.all.find(group => group.name === 'right_forearm');
  const cigarette = new Group({name: 'cigarette', origin: [-7, 13.5, -1.5]}).addTo(forearm).init();
  const context = texture.canvas.getContext('2d');
  const scale = texture.width / texture.uv_width;
  for (const [name, from, to, offset, color] of [
    ['cigarette_paper', [-7.5, 13, -5], [-6.5, 14, -1], [200, 200], '#e5e0cd'],
    ['cigarette_filter', [-7.5, 13, -1.7], [-6.5, 14, -0.7], [215, 200], '#b77d42'],
    ['cigarette_ember', [-7.5, 13, -5.35], [-6.5, 14, -4.35], [225, 200], '#d46535']
  ]) {
    context.fillStyle = color;
    context.fillRect(offset[0] * scale, offset[1] * scale, 12 * scale, 6 * scale);
    const cube = new Cube({name, from, to, inflate: -0.32, box_uv: true, uv_offset: offset}).addTo(cigarette).init();
    for (const face of Object.values(cube.faces)) face.texture = texture.uuid;
    created.push(cube);
  }
  texture.updateChangesAfterEdit();
  Canvas.updateAll();
  const unchanged = before.every(item => {
    const cube = Cube.all.find(element => element.uuid === item.uuid);
    return cube.from.every((value, index) => value === item.from[index]) && cube.to.every((value, index) => value === item.to[index]);
  });
  if (!unchanged) throw new Error('Rest pose changed');
  Undo.finishEdit('Add elbow rig and smoking prop', {elements: Cube.all.slice(), outliner: true, textures: [texture], bitmap: true});
  return JSON.stringify({restPoseUnchanged: unchanged, groups: Group.all.map(group => group.name), addedCubes: created.length});
})()