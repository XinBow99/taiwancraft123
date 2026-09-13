(() => {
  if (Project.name !== 'new_lambo') throw new Error('Wrong project');
  if (Cube.all.some(cube => cube.name === 'integrated_lamp_side_return')) throw new Error('Lamp installation already corrected');
  const filesystem = require('fs');
  const backup = Project.save_path.replace(/\.bbmodel$/, '.before-seated-lamps.bbmodel');
  if (!filesystem.existsSync(backup)) filesystem.writeFileSync(backup, Codecs.project.compile());
  Timeline.pause();
  Timeline.setTime(0);
  if (Animation.selected) Animator.preview();
  const lampNames = new Set(['triangular_headlamp_shell','recessed_optical_bed','headlamp_red_surround','headlamp_satin_bezel','led_dark_channel','flush_y_led','led_junction','projector_ring','projector_lens_center','lower_recess_fin','upper_reflector_edge']);
  const existing = Cube.all.slice();
  const owner = Group.all.find(group => group.name === 'front_fascia');
  const texture = Texture.all[0];
  const added = [];
  Undo.initEdit({elements: existing, outliner: true});
  for (const cube of existing) if (cube.name === 'front_corner_taper' || cube.name === 'outer_red_cheek') cube.remove();
  const originalNormal = new THREE.Vector3(0, 0.65, -1).normalize();
  const originalBase = new THREE.Vector3(0, 12, -93);
  const source = [[18,20.8],[37.5,30],[35,12.5]];
  const target = [[18,20.5,-86.5],[37.5,24,-70.7],[35,11.1,-89.4]];
  const determinant = (source[1][1]-source[2][1])*(source[0][0]-source[2][0])+(source[2][0]-source[1][0])*(source[0][1]-source[2][1]);
  const normal = new THREE.Vector3(...target[2]).sub(new THREE.Vector3(...target[0])).cross(new THREE.Vector3(...target[1]).sub(new THREE.Vector3(...target[0]))).normalize();
  if (normal.z > 0) normal.negate();
  const map = (point, side) => {
    const depth = point.clone().sub(originalBase).dot(originalNormal);
    const projected = point.clone().addScaledVector(originalNormal, -depth);
    const across = projected.x * side;
    const height = projected.y;
    const first = ((source[1][1]-source[2][1])*(across-source[2][0])+(source[2][0]-source[1][0])*(height-source[2][1]))/determinant;
    const second = ((source[2][1]-source[0][1])*(across-source[2][0])+(source[0][0]-source[2][0])*(height-source[2][1]))/determinant;
    const weights = [first,second,1-first-second];
    const position = new THREE.Vector3();
    for (let index=0;index<3;index++) position.addScaledVector(new THREE.Vector3(...target[index]),weights[index]);
    position.addScaledVector(normal,depth);
    position.x *= side;
    return position;
  };
  let transformed = 0;
  for (const cube of existing.filter(item => lampNames.has(item.name))) {
    const side = Math.sign(cube.origin[0]);
    const origin = new THREE.Vector3(...cube.origin);
    const quaternion = new THREE.Quaternion().setFromEuler(new THREE.Euler(...cube.rotation.map(value=>value*Math.PI/180),'ZYX'));
    const center = new THREE.Vector3(...cube.from.map((value,axis)=>(value+cube.to[axis])/2));
    const centerWorld = center.clone().sub(origin).applyQuaternion(quaternion).add(origin);
    const mappedCenter = map(centerWorld,side);
    const mappedAxis = axis => map(centerWorld.clone().add(axis.applyQuaternion(quaternion)),side).sub(mappedCenter);
    const axisX = mappedAxis(new THREE.Vector3(1,0,0)).normalize();
    const axisY = mappedAxis(new THREE.Vector3(0,1,0));
    axisY.addScaledVector(axisX,-axisY.dot(axisX)).normalize();
    const axisZ = axisX.clone().cross(axisY).normalize();
    const axes = [axisX,axisY,axisZ];
    const half = [0,0,0];
    for (const positionX of [cube.from[0],cube.to[0]]) for (const positionY of [cube.from[1],cube.to[1]]) for (const positionZ of [cube.from[2],cube.to[2]]) {
      const world = new THREE.Vector3(positionX,positionY,positionZ).sub(origin).applyQuaternion(quaternion).add(origin);
      const relative = map(world,side).sub(mappedCenter);
      axes.forEach((axis,index)=>half[index]=Math.max(half[index],Math.abs(relative.dot(axis))));
    }
    const angles = new THREE.Euler().setFromRotationMatrix(new THREE.Matrix4().makeBasis(...axes),'ZYX');
    cube.from = mappedCenter.toArray().map((value,axis)=>value-half[axis]);
    cube.to = mappedCenter.toArray().map((value,axis)=>value+half[axis]);
    cube.origin = mappedCenter.toArray();
    cube.rotation = [angles.x,angles.y,angles.z].map(value=>value*180/Math.PI);
    if (cube.name==='triangular_headlamp_shell') {
      cube.from[1] -= 3;
      const shift = axisY.clone().multiplyScalar(-1.5);
      const desiredCenter = mappedCenter.clone().add(shift);
      const dimensions = cube.to.map((value,axis)=>value-cube.from[axis]);
      cube.from = desiredCenter.toArray().map((value,axis)=>value-dimensions[axis]/2);
      cube.to = desiredCenter.toArray().map((value,axis)=>value+dimensions[axis]/2);
      cube.origin = desiredCenter.toArray();
    }
    transformed++;
  }
  const panel = (name, corners, material) => {
    const points=corners.map(point=>new THREE.Vector3(...point));
    const center=points.reduce((sum,point)=>sum.add(point),new THREE.Vector3()).multiplyScalar(.25);
    const across=points[1].clone().sub(points[0]).add(points[3].clone().sub(points[2])).multiplyScalar(.5);
    const along=points[2].clone().sub(points[0]).add(points[3].clone().sub(points[1])).multiplyScalar(.5);
    const width=across.length();across.normalize();along.addScaledVector(across,-along.dot(across));
    const length=along.length();along.normalize();
    const perpendicular=along.clone().cross(across).normalize();
    const rotation=new THREE.Euler().setFromRotationMatrix(new THREE.Matrix4().makeBasis(across,perpendicular,along),'ZYX');
    const dimensions=[width+.15,.8,length+.15];
    const cube=new Cube({name,from:center.toArray().map((value,axis)=>value-dimensions[axis]/2),to:center.toArray().map((value,axis)=>value+dimensions[axis]/2),origin:center.toArray(),rotation:[rotation.x,rotation.y,rotation.z].map(value=>value*180/Math.PI),box_uv:true,uv_offset:[material%4*512+4,Math.floor(material/4)*384+4]}).addTo(owner).init();
    for(const face of Object.values(cube.faces))face.texture=texture.uuid;
    added.push(cube);
  };
  const blend=(first,last,fraction)=>first.map((value,axis)=>value+(last[axis]-value)*fraction);
  for(const side of [-1,1]) {
    const mirror=point=>[point[0]*side,point[1],point[2]];
    const strips=[
      {name:'integrated_lamp_side_return',front:[target[1],target[2]],back:[[39,24,-68],[37,10,-78]],material:0},
      {name:'integrated_lamp_upper_return',front:[target[0],target[1]],back:[[19,22,-84],[37.5,25,-68]],material:0},
      {name:'integrated_lamp_lower_return',front:[target[0],target[2]],back:[[18,10,-87],[35,9,-87]],material:4}
    ];
    for(const strip of strips)for(let index=0;index<12;index++) {
      const start=index/12,end=(index+1)/12;
      panel(strip.name,[blend(...strip.front,start),blend(...strip.back,start),blend(...strip.front,end),blend(...strip.back,end)].map(mirror),strip.material);
    }
  }
  Canvas.updateAll();
  Undo.finishEdit('Seat complete headlights in body pockets',{elements:Cube.all.slice(),outliner:true});
  filesystem.writeFileSync(Project.save_path,Codecs.project.compile());
  Project.saved=true;
  Preview.selected.camera.position.set(105,47,-117);
  Preview.selected.controls.target.set(31,21,-77);
  Preview.selected.controls.update();
  return JSON.stringify({transformed,bodyReturnPanels:added.length,saved:Project.saved,normal:normal.toArray(),animations:Animation.all.length});
})()