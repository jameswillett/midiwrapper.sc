MIDIWrapper {
	classvar ccDict;
	classvar ccVals;
	classvar ccs;
	classvar chanToUse;
	classvar notes;

	*init {
		arg ins, insArgs = [], chan, source,
		    wheel = 2, wheelUp = wheel, wheelDown = wheel, ampMin = 0, ampMax = 1,
		    aftertouchArg = "", aftertouchMin = 0, aftertouchMax = 1,
		    ccArgs = [];
		//TODO: mod wheel
		var bend = 8192,
		    n = abs(Date.localtime.hash);

		ccVals = ();
		ccDict = ();
		ccs = ccArgs;
		notes = Array.fill(128, {nil});

		if (source.isNil, {
			MIDIIn.connectAll;
		}, {
			MIDIClient.init;
			MIDIIn.connect(source);
		});

		if (not(chan.isNil), { chanToUse = chan - 1 });

		MIDIdef.noteOn("MIDIWrapperOn_" ++ ins ++ "_" ++ n, {
			arg val, num, notechan;

			if (notes[num].isNil, {
				notes[num] = [nil, nil];
			}, {
				notes[num][0].set(\gate, 0);
			});

			notes[num][0] = Synth.new(ins, insArgs ++ [
				\freq, num.midicps,
				\gate, 1,
				\amp, val.linlin(0,127,ampMin,ampMax)
			] ++ ccVals.asSortedArray.flatten);

			notes[num][1] = bend == 8192;
		}, chan: chanToUse);

		MIDIdef.noteOff("MIDIWrapperOff_" ++ ins ++ "_" ++ n, {
			arg val, num, notechan;

			if (not(notes[num].isNil), {
				notes[num][0].set(
					\gate, 0,
					aftertouchArg, val.linlin(0, 127, aftertouchMin, aftertouchMax)
				);
			});
			notes[num] = nil;
		}, chan: chanToUse);

		MIDIdef.bend("MIDIWrapperBend_" ++ ins ++ "_" ++ n, {
			arg val;
			var bendToUse;

			bend = val;
			if (bend > 8192, {
				bendToUse = bend.linlin(8192, 16383, 0, wheelUp);
			}, {
				bendToUse = bend.linlin(0, 8192, neg(wheelDown), 0);
			});
			notes.do {
				arg instance, note;

				if (instance[1], { instance[0].set(\freq, (note + bendToUse).midicps) });
			}
		}, chan: chanToUse);

		MIDIdef.cc("MIDIWrapperCC_" ++ ins ++ "_" ++ n, {
			arg val, thisCC;

			ccs.do {
				arg row;
				var cc = row[0],
				    key = row[1],
			        min = row[2],
			        max = row[3];

				if (cc == thisCC, {
					notes.do {
						arg instance;
						var valScaled = val.linlin(0, 127, min, max);

						if (not(instance.isNil), {
							instance[0].set(key, valScaled);
							ccVals.add(key -> valScaled);

							// TODO: use ccDict
							ccDict.add(key -> (ccNum: cc, min: min, max: max))
						});
					}
				});
			}
		}, chan: chanToUse);
	}

	*updateCC {
		arg list = [];
		var d = ();

		d.putPairs(list);
		ccs.do {
			arg row;

			d.keysValuesDo {
				arg key, val;

				if (row[1] == key, {
					row[2] = val[0];
					row[3] = val[1];
				});
			}
		}
	}

	*removeCC {
		arg key;

		ccs = ccs.reject({
			arg row;

			row[1] == key;
		});
	}

	*addCC {
		arg row;

		ccs = ccs ++ [row];
	}
}