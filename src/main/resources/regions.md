```json5
{
  "spawn": {
    "key1": {
      "type": "surface",  // default as "surface", this defines which LocationProvider to use.
      "region": [         // can't be empty, or will skip this region.
        [0,100,100,100,200,200,200], // the first int defines which RegionProvider to use
        [0,200,200,300,300]
      ],
      "mob": [            // can't be empty, or will skip this region.
        {
          "prototype": "key1",  // can't be empty, or this mob will not spawn in this region.
          "weight": 30          // default as 10
        },
        {"prototype": "key2", "weight": 60},
        {"prototype": "key1"}
      ],
      "exclude": [        // default as empty.
        [0,150,150,150,250,250,250]
      ],
      "density": 20, // +- 32 from the center, including y.
      "world": "mcg_gensokyo" // 2
    }
  },
  "despawn": {
    "key1": {
      "world": 2,
      "region": [         // can't be empty, or this region will be skipped.
        [0,0,0,0,300,100,100]
      ],
      "exclude": [        // default as empty
        [0,0,0,0,200,100,100]
      ],
      "mob": ["*"]        // default as ["*"], empty will ignore this region.
    },
    "key2": {
      "world": "mcg_gensokyo",
      "region": [[0,1,2,3,4,5,6]],
      "exclude": [[0,2,3,4,5,6,7]],
      "mob": ["key1", "key2"]
    }
  }
}
```