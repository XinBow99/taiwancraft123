(async () => {
  if (Project.name !== 'bargarjung') throw new Error('Wrong project');
  const texture = Texture.all.find(item => item.name === 'bargarjung.png');
  if (!texture || texture.error) throw new Error('Missing character texture');
  const atlas = document.createElement('canvas');
  atlas.width = 1024;
  atlas.height = 1024;
  const context = atlas.getContext('2d');
  context.imageSmoothingEnabled = false;
  context.drawImage(texture.canvas, 0, 0, 1024, 1024);
  const garments = Cube.all.filter(element => element.name === 'oversized_black_wing_tee' || /^sleeve_-?1$/.test(element.name));
  for (const garment of garments) {
    for (const direction of ['north', 'south']) {
      const [startX, startY, endX, endY] = garment.faces[direction].uv;
      const width = Math.round((endX - startX) * 4);
      const height = Math.round((endY - startY) * 4);
      context.save();
      context.translate(startX * 4, startY * 4);
      context.beginPath();
      context.rect(0, 0, width, height);
      context.clip();
      const pixel = (positionX, positionY, sizeX, sizeY, color) => {
        context.fillStyle = color;
        context.fillRect(positionX, positionY, sizeX, sizeY);
      };
      pixel(0, 0, width, height, '#252629');
      for (let row = 0; row < height; row += 4) {
        for (let column = 0; column < width; column += 4) {
          pixel(column, row, 4, 4, (column + row) % 12 === 0 ? '#292a2c' : '#252629');
        }
      }
      const torso = garment.name === 'oversized_black_wing_tee';
      const wing = mirror => {
        const featherPixel = (positionX, positionY, sizeX, sizeY, color) => {
          pixel(mirror ? width - positionX - sizeX : positionX, positionY, sizeX, sizeY, color);
        };
        const feathers = torso ? 7 : 4;
        for (let feather = 0; feather < feathers; feather++) {
          const baseX = 1 + feather * 2;
          const baseY = 3 + feather;
          const length = torso ? 7 + feather * 3 : 6 + feather * 2;
          for (let step = 0; step < length; step++) {
            const bend = Math.floor(step / 7);
            const positionX = baseX + bend;
            const positionY = baseY + step;
            featherPixel(positionX, positionY, 3, 1, '#545962');
            featherPixel(positionX, positionY, step > length - 3 ? 1 : 2, 1, step % 6 === 0 ? '#c9cbd0' : '#a6abb4');
            if (step < length - 4 && step % 4 < 2) featherPixel(positionX + 1, positionY, 1, 1, '#e0dfdc');
          }
        }
        for (let covert = 0; covert < (torso ? 6 : 4); covert++) {
          featherPixel(covert * 2, 2 + covert, 3, 2, '#d2d3d3');
          featherPixel(covert * 2 + 1, 4 + covert, 2, 2, '#737b86');
        }
      };
      wing(!torso && garment.name === 'sleeve_1');
      if (torso) {
        wing(true);
        pixel(14, 0, 12, 2, '#131517');
        pixel(16, 2, 8, 2, '#181a1d');
        pixel(0, height - 2, width, 1, '#16181a');
      }
      context.restore();
    }
  }
  Undo.initEdit({textures: [texture], bitmap: true});
  texture.fromDataURL(atlas.toDataURL('image/png'));
  texture.uv_width = 256;
  texture.uv_height = 256;
  await new Promise(resolve => setTimeout(resolve, 200));
  Canvas.updateAll();
  Undo.finishEdit('Refine silver feather shirt');
  const filesystem = require('fs');
  const texturePath = Project.save_path.replace(/\.bbmodel$/, '.png');
  filesystem.writeFileSync(texturePath, Buffer.from(atlas.toDataURL('image/png').split(',')[1], 'base64'));
  texture.path = texturePath;
  filesystem.writeFileSync(Project.save_path, Codecs.project.compile());
  Project.saved = true;
  Preview.selected.setProjectionMode(false);
  Preview.selected.camera.position.set(25, 23, -65);
  Preview.selected.controls.target.set(0, 17, 0);
  Preview.selected.controls.update();
  return JSON.stringify({saved: Project.saved, resolution: [texture.width, texture.height], garments: garments.length});
})()