(() => {
  if (Project.name !== 'new_lambo') throw new Error('Wrong project');
  if (Cube.all.some(cube => cube.name === 'hood_shoulder_fill')) throw new Error('Already refined');
  const body = Group.all.find(group => group.name === 'chassis');
  const texture = Texture.all.find(item => item.name === 'new_lambo.png');
  const created = [];
  Undo.initEdit({elements: created, outliner: true});
  const add = (name, from, to, offset) => {
    const cube = new Cube({name, from, to, box_uv: true, uv_offset: offset}).addTo(body).init();
    for (const face of Object.values(cube.faces)) face.texture = texture.uuid;
    created.push(cube);
  };
  for (let positionZ = -78; positionZ < -20; positionZ += 2) {
    const progress = (positionZ + 84) / 64;
    const hoodTop = 20 + progress * 13;
    const inner = 27 - progress * 3;
    const fenderTop = positionZ < -60 ? 24 + (positionZ + 82) * 0.45 : 35.5;
    for (const side of [-1, 1]) {
      for (let strip = 0; strip < 3; strip++) {
        const start = inner + (29.5 - inner) * strip / 3;
        const end = inner + (29.5 - inner) * (strip + 1) / 3;
        const top = hoodTop + (fenderTop - hoodTop) * (strip + 1) / 3;
        add('hood_shoulder_fill', [side < 0 ? -end : start, top - 2, positionZ], [side < 0 ? -start : end, top, positionZ + 2], [4, 4]);
      }
    }
  }
  for (const side of [-1, 1]) {
    add('cabin_shoulder_rail', [side < 0 ? -38 : 30, 30, -24], [side < 0 ? -30 : 38, 33, 27], [516, 4]);
    add('headlamp_full_backing', [side < 0 ? -36 : 19, 18, -80.4], [side < 0 ? -19 : 36, 28, -78.5], [4, 388]);
  }
  Canvas.updateAll();
  Undo.finishEdit('Close hood shoulders and finish lamp housings');
  const filesystem = require('fs');
  filesystem.writeFileSync(Project.save_path, Codecs.project.compile());
  Project.saved = true;
  return JSON.stringify({added: created.length, saved: true, cubeCount: Cube.all.length});
})()