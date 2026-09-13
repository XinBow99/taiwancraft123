(() => {
  if (Project.name !== 'new_lambo') throw new Error('Wrong project');
  const targets = [[18,20.5,-86.5],[37.5,24,-70.7],[35,11.1,-89.4]];
  const normal = new THREE.Vector3(.322734080001,.758379606218,-.566306530493);
  const inspect = () => {
    scene.updateMatrixWorld(true);
    const visible = Cube.all.filter(cube=>cube.visibility);
    const byMesh = new Map(visible.map(cube=>[cube.mesh,cube]));
    const meshes = visible.map(cube=>cube.mesh);
    const blockers = new Set();
    let samples = 0;
    for (const side of [-1,1]) for (let row=1;row<20;row++) for (let column=1;column<20-row;column++) {
      const weights = [row/20,column/20,1-(row+column)/20];
      const point = new THREE.Vector3();
      targets.forEach((target,index)=>point.addScaledVector(new THREE.Vector3(...target),weights[index]));
      point.x *= side;
      const outward = normal.clone();
      outward.x *= side;
      const hit = new THREE.Raycaster(point.clone().addScaledVector(outward,30),outward.clone().negate(),0,35).intersectObjects(meshes,false)[0];
      if (hit) {
        const cube = byMesh.get(hit.object);
        if (!/headlamp|optical|led_|flush_y|projector|recess_fin|reflector/.test(cube.name)) blockers.add(cube);
      }
      samples++;
    }
    return {blockers,samples};
  };
  const initial = inspect();
  const removable = new Set(['lower_front_intake','convex_hood_red_transition']);
  if ([...initial.blockers].some(cube=>!removable.has(cube.name))) throw new Error('Unexpected lamp obstruction');
  Undo.initEdit({elements:[...initial.blockers],outliner:true});
  for (const cube of initial.blockers) cube.remove();
  Canvas.updateAll();
  Undo.finishEdit('Trim old panels inside recessed lamp openings');
  const final = inspect();
  if (final.blockers.size) throw new Error(JSON.stringify([...final.blockers].map(cube=>cube.name)));
  return JSON.stringify({removed:initial.blockers.size,samples:final.samples,bodyObstructions:final.blockers.size});
})()