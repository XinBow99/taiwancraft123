(async () => {
  if (Project.name !== 'bargarjung') throw new Error('Wrong project');
  const texture = Texture.all.find(item => item.name === 'bargarjung.png');
  const right = Cube.all.find(element => element.name === 'sleeve_-1');
  const left = Cube.all.find(element => element.name === 'sleeve_1');
  if (!texture || texture.error || !right || !left) throw new Error('Missing sleeves or texture');
  const atlas = document.createElement('canvas');
  atlas.width = texture.width;
  atlas.height = texture.height;
  const context = atlas.getContext('2d');
  context.drawImage(texture.canvas, 0, 0);
  const scaleX = texture.width / texture.uv_width;
  const scaleY = texture.height / texture.uv_height;
  const checks = [];
  for (const direction of ['north', 'south']) {
    const sourceUV = right.faces[direction].uv;
    const targetUV = left.faces[direction].uv;
    const width = (sourceUV[2] - sourceUV[0]) * scaleX;
    const height = (sourceUV[3] - sourceUV[1]) * scaleY;
    if (width !== (targetUV[2] - targetUV[0]) * scaleX || height !== (targetUV[3] - targetUV[1]) * scaleY) {
      throw new Error('Sleeve UV dimensions differ');
    }
    const source = context.getImageData(sourceUV[0] * scaleX, sourceUV[1] * scaleY, width, height);
    const mirrored = context.createImageData(width, height);
    for (let row = 0; row < height; row++) {
      for (let column = 0; column < width; column++) {
        const sourceIndex = (row * width + width - column - 1) * 4;
        const targetIndex = (row * width + column) * 4;
        mirrored.data.set(source.data.subarray(sourceIndex, sourceIndex + 4), targetIndex);
      }
    }
    context.putImageData(mirrored, targetUV[0] * scaleX, targetUV[1] * scaleY);
    const actual = context.getImageData(targetUV[0] * scaleX, targetUV[1] * scaleY, width, height);
    if (actual.data.some((value, index) => value !== mirrored.data[index])) throw new Error('Mirror validation failed');
    checks.push({direction, mirrorMismatches: 0});
  }
  Undo.initEdit({textures: [texture], bitmap: true});
  const texturePath = texture.path;
  texture.fromDataURL(atlas.toDataURL('image/png'));
  await new Promise(resolve => setTimeout(resolve, 200));
  Canvas.updateAll();
  Undo.finishEdit('Mirror left sleeve feathers from right');
  const filesystem = require('fs');
  texture.path = texturePath;
  filesystem.writeFileSync(texturePath, Buffer.from(atlas.toDataURL('image/png').split(',')[1], 'base64'));
  filesystem.writeFileSync(Project.save_path, Codecs.project.compile());
  Project.saved = true;
  return JSON.stringify({saved: Project.saved, checks});
})()