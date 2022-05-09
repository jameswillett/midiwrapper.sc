# midiwrapper.sc
A wrapper for Supercollider synths to reduce MIDI boilerplate

heres a synth i definitely did not steal from an eli fieldsteel video
```supercollider
(
SynthDef.new(\tone, {
	arg freq=440, amp=0.2, vibamp=0.3, gate=0, out=0;
	var sig, env, vib;

	vib = SinOsc.kr(7).bipolar(vibamp);
	vib = vib.midiratio;

	sig = SinOsc.ar(freq * vib, 0, amp!2);
	env = EnvGen.kr(
		Env.adsr(releaseTime: 1),
		gate,
		doneAction:2
	);
	sig = sig * env;
	Out.ar(out, sig);
}).add;
)
```

initialize
ccArgs is an array of "tuples":
```
[[ 
	cc number, 
	arg to be updated, 
	min (the value of the arg at midi value 0),
	max (the value of the arg at midi value 127)
]]
```
TODO: Currently the scaling is only linear, other scalings to be supported
```supercollider
a = MIDIWrapper.init(\tone, ccArgs: [[4, \vibamp, 0, 8]]);
```
update 1+ cc scaling(s) [ arg, [ min, max ], arg, [ min, max ] ...]
```supercollider
a.updateCC([\vibamp, [2, 5]]);
```
remove cc scaling without removing association
```supercollider
a.updateCC([\vibamp, [0, 0]]);
```
remove cc association without necessarily removing scaling
```supercollider
a.removeCC(\vibamp);
```
add new scaling
```supercollider
a.addCC([4, \vibamp, 0, 8]);
```
