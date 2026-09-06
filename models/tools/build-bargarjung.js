(async () => {
  if (Project.name !== 'bargarjung' || Outliner.elements.length !== 0) {
    throw new Error('Expected the empty bargarjung project');
  }
  const filesystem = require('fs');
  const projectPath = Project.save_path;
  if (!projectPath) throw new Error('The project must have a save path');
  const backupPath = projectPath.replace(/\.bbmodel$/, '.before-reference.bbmodel');
  if (!filesystem.existsSync(backupPath)) {
    filesystem.writeFileSync(backupPath, Codecs.project.compile());
  }
  const created = [];
  Undo.initEdit({elements: created, outliner: true, textures: [], uv_mode: true});
  Project.texture_width = 256;
  Project.texture_height = 256;
  Project.box_uv = true;
  const atlas = document.createElement('canvas');
  atlas.width = 512;
  atlas.height = 512;
  const context = atlas.getContext('2d');
  context.imageSmoothingEnabled = false;
  const materials = [];
  let cursorX = 2;
  let cursorY = 2;
  let rowHeight = 0;
  let seed = 89;
  const random = () => {
    seed = (Math.imul(seed, 1664525) + 1013904223) >>> 0;
    return seed / 4294967296;
  };
  const groups = {};
  const group = (name, origin, parent) => {
    const result = new Group({name, origin});
    if (parent) result.addTo(parent);
    result.init();
    groups[name] = result;
    return result;
  };
  const root = group('bargarjung', [0, 0, 0]);
  const body = group('body', [0, 24, 0], root);
  const head = group('head', [0, 25, 0], root);
  const rightArm = group('right_arm', [-5, 24, 0], root);
  const leftArm = group('left_arm', [5, 24, 0], root);
  const rightLeg = group('right_leg', [-2.5, 14, 0], root);
  const leftLeg = group('left_leg', [2.5, 14, 0], root);
  const cube = (name, from, to, parent, material, extra = {}) => {
    const width = Math.max(1, Math.floor(to[0] - from[0]));
    const height = Math.max(1, Math.floor(to[1] - from[1]));
    const depth = Math.max(1, Math.floor(to[2] - from[2]));
    const tileWidth = 2 * (width + depth);
    const tileHeight = height + depth;
    if (cursorX + tileWidth + 2 > 256) {
      cursorX = 2;
      cursorY += rowHeight + 2;
      rowHeight = 0;
    }
    if (cursorY + tileHeight + 2 > 256) throw new Error('Texture atlas overflow');
    const offset = [cursorX, cursorY];
    const element = new Cube({name, from, to, box_uv: true, uv_offset: offset, ...extra});
    element.addTo(parent).init();
    created.push(element);
    materials.push({element, material, offset, width, height, depth});
    cursorX += tileWidth + 2;
    rowHeight = Math.max(rowHeight, tileHeight);
    return element;
  };
  cube('oversized_black_wing_tee', [-5, 14, -2], [5, 25, 3], body, 'shirt');
  cube('ribbed_shirt_hem', [-5, 14, -2], [5, 15, 3], body, 'hem', {inflate: 0.08});
  cube('neck', [-2, 24, -1], [2, 27, 2], head, 'skin');
  cube('face', [-4, 26, -3], [4, 33, 3], head, 'face');
  cube('ear_right', [-5, 27, -1], [-4, 29, 1], head, 'ear');
  cube('ear_left', [4, 27, -1], [5, 29, 1], head, 'ear');
  cube('nose', [-0.5, 28, -3.55], [0.5, 29, -3], head, 'skin');
  cube('undercut', [-4, 30, -2], [4, 33, 3], head, 'hairSide', {inflate: 0.04});
  cube('hair_crown', [-3, 32, -2], [3, 34, 3], head, 'hair');
  for (let column = 0; column < 8; column++) {
    const hairX = column - 4;
    const fringeBottom = [31, 30, 31, 30, 31, 31, 30, 31][column];
    cube(`fringe_${column}`, [hairX, fringeBottom, -3.3], [hairX + 1, 33 + (column % 3 === 1 ? 1 : 0), -2.3], head, 'hair');
  }
  for (let row = 0; row < 3; row++) {
    for (let column = 0; column < 4; column++) {
      const hairX = -4 + column * 2 + (row === 1 ? 0.25 : 0);
      const hairZ = -2 + row * 2;
      const hairY = 33 + ((column + row) % 3 === 0 ? 1 : 0);
      cube(`textured_top_${row}_${column}`, [hairX, 32, hairZ], [hairX + 2, hairY + 1, hairZ + 2], head, 'hair');
    }
  }
  for (const side of [-1, 1]) {
    const arm = side < 0 ? rightArm : leftArm;
    const leg = side < 0 ? rightLeg : leftLeg;
    const armMin = side < 0 ? -9 : 5;
    const legMin = side < 0 ? -5 : 0;
    cube(`sleeve_${side}`, [armMin, 19, -2], [armMin + 4, 25, 3], arm, 'sleeve');
    cube(`sleeve_cuff_${side}`, [armMin, 19, -2], [armMin + 4, 20, 3], arm, 'hem', {inflate: 0.07});
    cube(`tattoo_forearm_${side}`, [armMin + 0.5, 14, -1.5], [armMin + 3.5, 19, 2.5], arm, 'tattoo');
    cube(`hand_${side}`, [armMin + 0.5, 12, -1.5], [armMin + 3.5, 14, 2.5], arm, 'hand');
    const thumbX = side < 0 ? armMin + 3.1 : armMin - 0.1;
    cube(`thumb_${side}`, [thumbX, 12.5, -1.8], [thumbX + 1, 14.3, -0.3], arm, 'skin');
    cube(`distressed_denim_${side}`, [legMin, 2, -2], [legMin + 5, 14, 3], leg, side < 0 ? 'denimRight' : 'denimLeft');
    cube(`jeans_cuff_${side}`, [legMin, 1, -2], [legMin + 5, 3, 3], leg, 'denimCuff', {inflate: 0.1});
    cube(`cuff_fold_${side}`, [legMin + 0.2, 2, -2.15], [legMin + 4.8, 2.7, -1.9], leg, 'denimCuff');
    cube(`blue_slide_sole_${side}`, [legMin, 0, -4], [legMin + 5, 1, 3], leg, 'blue');
    cube(`foot_${side}`, [legMin + 0.35, 1, -3.7], [legMin + 4.65, 2, 1], leg, 'foot');
    cube(`white_slide_strap_${side}`, [legMin + 0.1, 1.8, -2.3], [legMin + 4.9, 2.65, 0.2], leg, 'white');
    cube(`blue_strap_edge_${side}`, [legMin, 1.7, -2.6], [legMin + 5, 2.5, -2.1], leg, 'blue');
  }
  const chain = [[-2.2,24.7],[-2.0,24.1],[-1.6,23.6],[-1.1,23.2],[-0.55,22.95],[0,22.85],[0.55,22.95],[1.1,23.2],[1.6,23.6],[2,24.1],[2.2,24.7]];
  for (let index = 0; index < chain.length; index++) {
    const [chainX, chainY] = chain[index];
    cube(`silver_chain_link_${index}`, [chainX - 0.22, chainY - 0.22, -2.42], [chainX + 0.22, chainY + 0.22, -2.08], body, index % 2 ? 'silverDark' : 'silver');
  }
  const palettes = {
    shirt: ['#252629','#292a2d','#222326','#2c2d30'],
    hem: ['#202124','#242528','#28292b'],
    sleeve: ['#252629','#2b2c2e','#222326'],
    skin: ['#dba77f','#dfaE86','#d5a07a','#e5b68e'],
    face: ['#e2b18b','#e3b38d','#dfac86'],
    ear: ['#d4a07a','#e2af86','#c48c66'],
    tattoo: ['#d6a27b','#dca981','#d2a07b'],
    hand: ['#dfad83','#e5b58c','#d8a57c'],
    foot: ['#e2b38c','#dca982','#e7b993'],
    hair: ['#17181a','#202123','#27282a','#1b1c1e'],
    hairSide: ['#303031','#373534','#29292a'],
    denimRight: ['#62738c','#708099','#54657e','#7e8ba0','#495a73'],
    denimLeft: ['#66768e','#78869b','#566880','#8590a2','#4c5d76'],
    denimCuff: ['#687b94','#8392a5','#4a5e78','#71839a'],
    blue: ['#183a73','#204b8a','#2c5794','#254780'],
    white: ['#e5e7e7','#f2f1e9','#c9d0d8'],
    silver: ['#e1e1dc','#bfc3c5','#f2efe3'],
    silverDark: ['#939a9f','#d0d3d3','#b4b8ba']
  };
  const paintFace = (material, direction, startX, startY, faceWidth, faceHeight) => {
    const originX = Math.round(startX * 2);
    const originY = Math.round(startY * 2);
    const width = Math.max(1, Math.round(faceWidth * 2));
    const height = Math.max(1, Math.round(faceHeight * 2));
    const rect = (positionX, positionY, sizeX, sizeY, color) => {
      context.fillStyle = color;
      context.fillRect(originX + positionX, originY + positionY, sizeX, sizeY);
    };
    context.save();
    context.beginPath();
    context.rect(originX, originY, width, height);
    context.clip();
    const palette = palettes[material];
    rect(0, 0, width, height, palette[0]);
    for (let pixelY = 0; pixelY < height; pixelY += 2) {
      for (let pixelX = 0; pixelX < width; pixelX += 2) {
        rect(pixelX, pixelY, 2, 2, palette[Math.floor(random() * palette.length)]);
      }
    }
    if (material === 'face' && direction === 'north') {
      rect(0, 0, width, height, '#e1ae86');
      rect(1, 2, 14, 8, '#e6b58e');
      rect(0, 10, 2, 4, '#c9946d');
      rect(14, 10, 2, 4, '#c9946d');
      rect(2, 5, 4, 1, '#282624');
      rect(3, 6, 3, 1, '#282624');
      rect(10, 5, 4, 1, '#282624');
      rect(10, 6, 3, 1, '#282624');
      rect(2, 7, 4, 2, '#f4e8d8');
      rect(10, 7, 4, 2, '#f4e8d8');
      rect(4, 7, 2, 2, '#262323');
      rect(10, 7, 2, 2, '#262323');
      rect(2, 7, 4, 1, '#3b3028');
      rect(10, 7, 4, 1, '#3b3028');
      rect(7, 9, 2, 2, '#d49a71');
      rect(6, 12, 4, 1, '#a56f52');
      rect(7, 13, 3, 1, '#edc19d');
    }
    if (material === 'hair' || material === 'hairSide') {
      rect(0, height - 1, width, 1, '#141517');
      rect(0, 0, width, 1, material === 'hair' ? '#323335' : '#45413d');
    }
    if ((material === 'shirt' || material === 'sleeve') && (direction === 'north' || direction === 'south')) {
      const wing = (mirror) => {
        const wingPixel = (positionX, positionY, sizeX, sizeY, color) => {
          rect(mirror ? width - positionX - sizeX : positionX, positionY, sizeX, sizeY, color);
        };
        const offset = material === 'shirt' ? 3 : 0;
        const featherCount = material === 'shirt' ? 7 : 4;
        for (let feather = 0; feather < featherCount; feather++) {
          const positionX = 1 + feather;
          const positionY = offset + Math.floor(feather * 0.8);
          const length = Math.max(3, 11 - feather);
          wingPixel(positionX, positionY, 2, length, '#646970');
          wingPixel(positionX, positionY, 1, length - 1, '#c1c3c5');
          wingPixel(positionX + 1, positionY + 2, 1, length - 4, '#92979e');
          wingPixel(positionX, positionY, 2, 1, '#e0deda');
        }
        wingPixel(1, offset + 1, 5, 1, '#deddd7');
        wingPixel(2, offset, 3, 1, '#aaadb1');
      };
      wing(false);
      if (material === 'shirt') wing(true);
      if (material === 'shirt') {
        rect(7, 0, 6, 1, '#131517');
        rect(8, 1, 4, 1, '#151719');
        rect(0, height - 2, width, 1, '#17191b');
      }
    }
    if (material === 'tattoo' && direction !== 'up' && direction !== 'down') {
      for (let motif = 0; motif < 4; motif++) {
        const positionX = (motif % 2) * 3 + (direction === 'south' ? 1 : 0);
        const positionY = motif * 3 - 1;
        rect(positionX + 1, positionY, 2, 1, '#57554b');
        rect(positionX, positionY + 1, 1, 2, '#454941');
        rect(positionX + 2, positionY + 1, 1, 2, '#606052');
        rect(positionX + 1, positionY + 3, 2, 1, '#4d5147');
        rect(positionX + 1, positionY + 1, 1, 1, '#86816a');
      }
    }
    if (material.startsWith('denim')) {
      rect(0, 0, 1, height, '#a0a9b5');
      rect(width - 1, 0, 1, height, '#465971');
      for (let fold = 3; fold < height; fold += 5) {
        rect(1, fold, 3 + Math.floor(random() * 4), 1, '#94a0b0');
        rect(2, fold + 1, 4, 1, '#52647e');
      }
      if (material !== 'denimCuff' && direction === 'north') {
        rect(1, 1, width - 2, 1, '#394a61');
        rect(2, 0, 1, 4, '#a0aabc');
        rect(width - 3, 0, 1, 3, '#99a7b8');
        rect(1, 3, 3, 1, '#9aa6b6');
        rect(2, 4, 2, 1, '#9aa6b6');
        const tears = material === 'denimRight' ? [[3,8,5],[1,14,7],[4,20,4]] : [[2,6,4],[3,12,6],[1,19,6]];
        for (const [tearX, tearY, tearWidth] of tears) {
          rect(tearX, tearY, tearWidth, 4, '#343f50');
          rect(tearX + 1, tearY + 1, tearWidth - 2, 2, '#c2997d');
          rect(tearX - 1, tearY, tearWidth, 1, '#d6d5cd');
          rect(tearX, tearY + 3, tearWidth, 1, '#efeadc');
          rect(tearX + 1, tearY + 1, 2, 1, '#e4e0d6');
          rect(tearX + tearWidth - 2, tearY + 2, 3, 1, '#bac1c8');
          rect(tearX - 1, tearY + 4, 2, 1, '#adb7c4');
        }
      }
      if (material !== 'denimCuff' && direction === 'south') {
        rect(2, 2, width - 4, 1, '#a0aabb');
        rect(2, 3, 1, 4, '#a0aabb');
        rect(width - 3, 3, 1, 4, '#a0aabb');
        rect(3, 7, width - 6, 1, '#a0aabb');
      }
    }
    if ((material === 'hand' || material === 'foot') && (direction === 'north' || direction === 'up')) {
      for (let finger = 2; finger < width; finger += 2) {
        rect(finger, material === 'foot' ? 0 : height - 2, 1, material === 'foot' ? 2 : 2, '#b98562');
      }
    }
    if (material === 'blue') rect(0, height - 1, width, 1, '#10284f');
    context.restore();
  };
  for (const {material, offset, width, height, depth} of materials) {
    const [offsetX, offsetY] = offset;
    paintFace(material, 'east', offsetX, offsetY + depth, depth, height);
    paintFace(material, 'north', offsetX + depth, offsetY + depth, width, height);
    paintFace(material, 'west', offsetX + depth + width, offsetY + depth, depth, height);
    paintFace(material, 'south', offsetX + depth * 2 + width, offsetY + depth, width, height);
    paintFace(material, 'up', offsetX + depth, offsetY, width, depth);
    paintFace(material, 'down', offsetX + depth + width, offsetY, width, depth);
  }
  const texture = new Texture({name: 'bargarjung.png'});
  texture.fromDataURL(atlas.toDataURL('image/png')).add(false);
  texture.uv_width = 256;
  texture.uv_height = 256;
  for (const element of created) {
    for (const face of Object.values(element.faces)) face.texture = texture.uuid;
  }
  await new Promise(resolve => setTimeout(resolve, 200));
  Canvas.updateAll();
  Undo.finishEdit('Build reference bargarjung NPC', {elements: created, outliner: true, textures: [texture], uv_mode: true});
  const texturePath = projectPath.replace(/\.bbmodel$/, '.png');
  filesystem.writeFileSync(texturePath, Buffer.from(atlas.toDataURL('image/png').split(',')[1], 'base64'));
  texture.path = texturePath;
  filesystem.writeFileSync(projectPath, Codecs.project.compile());
  Project.saved = true;
  if (Preview.selected) {
    Preview.selected.camera.position.set(48, 31, -72);
    Preview.selected.controls.target.set(0, 17, 0);
    Preview.selected.controls.update();
  }
  return JSON.stringify({project: Project.name, cubes: created.length, groups: Group.all.length, texture: [atlas.width, atlas.height], atlasBottom: cursorY + rowHeight, saved: projectPath, backup: backupPath});
})()