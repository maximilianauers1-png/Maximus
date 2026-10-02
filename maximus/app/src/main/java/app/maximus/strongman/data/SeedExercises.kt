package app.maximus.strongman.data

import app.maximus.strongman.domain.EventMode

object ExerciseCategory {
    const val SQUAT = "SQUAT"
    const val BENCH = "BENCH"
    const val DEADLIFT = "DEADLIFT"
    const val PRESS = "PRESS"
    const val STONE = "STONE"
    const val CARRY = "CARRY"
    const val EVENT = "EVENT"
    const val ACCESSORY = "ACCESSORY"

    val all = listOf(SQUAT, BENCH, DEADLIFT, PRESS, STONE, CARRY, EVENT, ACCESSORY)
}

/** Seed keys of the lifts on the lift-ratio radar chart. */
object RadarLifts {
    val keys = listOf("back_squat", "bench_press", "deadlift_conventional", "strict_press", "log_clean_press")
}

/**
 * 147 seed exercises: 49 base lifts and events, 18 ordinary-gym accessories added with the conjugate
 * templates, and 80 further strongman implements, lift variants and accessories added in P4.
 * Seed keys are stable; the user may rename an exercise without breaking the templates.
 */
object SeedExercises {
    private fun e(key: String, de: String, en: String, cat: String, event: Boolean, mode: EventMode) =
        ExerciseEntity(seedKey = key, nameDe = de, nameEn = en, category = cat, isEvent = event, defaultMode = mode.name, notes = "")

    val all: List<ExerciseEntity> = listOf(
        e("atlas_stone_over_bar", "Atlas Stone über die Stange", "Atlas stone over bar", ExerciseCategory.STONE, true, EventMode.MAX_REPS),
        e("atlas_stone_platform", "Atlas Stone auf Podest", "Atlas stone to platform", ExerciseCategory.STONE, true, EventMode.MAX_HEIGHT),
        e("atlas_stone_series", "Stein-Serie", "Atlas stone series", ExerciseCategory.STONE, true, EventMode.FOR_TIME),
        e("stone_to_shoulder", "Stein zur Schulter", "Stone to shoulder", ExerciseCategory.STONE, true, EventMode.MAX_REPS),
        e("husafell_carry", "Husafell-Stein tragen", "Husafell stone carry", ExerciseCategory.STONE, true, EventMode.MAX_DISTANCE),
        e("log_clean_press", "Log Clean & Press", "Log clean and press", ExerciseCategory.PRESS, true, EventMode.MAX_LOAD),
        e("axle_clean_press", "Axle Clean & Press", "Axle clean and press", ExerciseCategory.PRESS, true, EventMode.MAX_LOAD),
        e("axle_press", "Axle-Drücken aus dem Rack", "Axle press from rack", ExerciseCategory.PRESS, true, EventMode.MAX_REPS),
        e("circus_dumbbell", "Circus-Kurzhantel", "Circus dumbbell", ExerciseCategory.PRESS, true, EventMode.MAX_REPS),
        e("viking_press", "Viking Press", "Viking press", ExerciseCategory.PRESS, true, EventMode.MAX_REPS),
        e("keg_toss", "Fass-Wurf über die Stange", "Keg toss over bar", ExerciseCategory.EVENT, true, EventMode.MAX_HEIGHT),
        e("farmers_walk", "Farmers Walk", "Farmers walk", ExerciseCategory.CARRY, true, EventMode.FOR_TIME),
        e("yoke_carry", "Yoke", "Yoke carry", ExerciseCategory.CARRY, true, EventMode.FOR_TIME),
        e("keg_carry", "Fass tragen", "Keg carry", ExerciseCategory.CARRY, true, EventMode.FOR_TIME),
        e("sandbag_carry", "Sandsack tragen", "Sandbag carry", ExerciseCategory.CARRY, true, EventMode.FOR_TIME),
        e("sandbag_over_bar", "Sandsack über die Stange", "Sandbag over bar", ExerciseCategory.EVENT, true, EventMode.MAX_REPS),
        e("frame_carry", "Rahmen tragen", "Frame carry", ExerciseCategory.CARRY, true, EventMode.FOR_TIME),
        e("conan_wheel", "Conans Rad", "Conan's wheel", ExerciseCategory.CARRY, true, EventMode.MAX_DISTANCE),
        e("tire_flip", "Reifen-Flip", "Tire flip", ExerciseCategory.EVENT, true, EventMode.FOR_TIME),
        e("sled_push", "Schlitten schieben", "Sled push", ExerciseCategory.EVENT, true, EventMode.FOR_TIME),
        e("sled_drag", "Schlitten ziehen", "Sled drag", ExerciseCategory.EVENT, true, EventMode.FOR_TIME),
        e("truck_pull", "Truck Pull (Geschirr)", "Truck pull (harness)", ExerciseCategory.EVENT, true, EventMode.FOR_TIME),
        e("arm_over_arm_pull", "Arm-über-Arm-Zug", "Arm-over-arm pull", ExerciseCategory.EVENT, true, EventMode.FOR_TIME),
        e("hercules_hold", "Herkules-Halten", "Hercules hold", ExerciseCategory.EVENT, true, EventMode.MAX_HOLD_TIME),
        e("car_deadlift", "Auto-Kreuzheben", "Car deadlift", ExerciseCategory.DEADLIFT, true, EventMode.MAX_REPS),
        e("deadlift_18inch", "18-Zoll-Kreuzheben", "18-inch deadlift", ExerciseCategory.DEADLIFT, true, EventMode.MAX_LOAD),
        e("deadlift_axle", "Axle-Kreuzheben", "Axle deadlift", ExerciseCategory.DEADLIFT, true, EventMode.MAX_LOAD),
        e("deadlift_conventional", "Kreuzheben konventionell", "Conventional deadlift", ExerciseCategory.DEADLIFT, false, EventMode.MAX_LOAD),
        e("deadlift_sumo", "Kreuzheben Sumo", "Sumo deadlift", ExerciseCategory.DEADLIFT, false, EventMode.MAX_LOAD),
        e("deadlift_deficit", "Defizit-Kreuzheben", "Deficit deadlift", ExerciseCategory.DEADLIFT, false, EventMode.MAX_LOAD),
        e("deadlift_trap_bar", "Trap-Bar-Kreuzheben", "Trap bar deadlift", ExerciseCategory.DEADLIFT, false, EventMode.MAX_LOAD),
        e("romanian_deadlift", "Rumänisches Kreuzheben", "Romanian deadlift", ExerciseCategory.DEADLIFT, false, EventMode.MAX_LOAD),
        e("back_squat", "Kniebeuge", "Back squat", ExerciseCategory.SQUAT, false, EventMode.MAX_LOAD),
        e("front_squat", "Frontkniebeuge", "Front squat", ExerciseCategory.SQUAT, false, EventMode.MAX_LOAD),
        e("zercher_squat", "Zercher-Kniebeuge", "Zercher squat", ExerciseCategory.SQUAT, false, EventMode.MAX_LOAD),
        e("box_squat", "Box-Kniebeuge", "Box squat", ExerciseCategory.SQUAT, false, EventMode.MAX_LOAD),
        e("safety_bar_squat", "Safety-Bar-Kniebeuge", "Safety bar squat", ExerciseCategory.SQUAT, false, EventMode.MAX_LOAD),
        e("bench_press", "Bankdrücken", "Bench press", ExerciseCategory.BENCH, false, EventMode.MAX_LOAD),
        e("close_grip_bench", "Enges Bankdrücken", "Close-grip bench press", ExerciseCategory.BENCH, false, EventMode.MAX_LOAD),
        e("incline_bench", "Schrägbankdrücken", "Incline bench press", ExerciseCategory.BENCH, false, EventMode.MAX_LOAD),
        e("floor_press", "Floor Press", "Floor press", ExerciseCategory.BENCH, false, EventMode.MAX_LOAD),
        e("strict_press", "Überkopfdrücken strikt", "Strict overhead press", ExerciseCategory.PRESS, false, EventMode.MAX_LOAD),
        e("push_press", "Push Press", "Push press", ExerciseCategory.PRESS, false, EventMode.MAX_LOAD),
        e("push_jerk", "Push Jerk", "Push jerk", ExerciseCategory.PRESS, false, EventMode.MAX_LOAD),
        e("z_press", "Z-Press", "Z press", ExerciseCategory.PRESS, false, EventMode.MAX_LOAD),
        e("power_clean", "Power Clean", "Power clean", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("barbell_row", "Langhantelrudern", "Barbell row", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("pull_up", "Klimmzug", "Pull-up", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("good_morning", "Good Morning", "Good morning", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        // Accessories available in any ordinary gym (added with the conjugate templates).
        e("db_bench_press", "Kurzhantel-Bankdrücken", "Dumbbell bench press", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("db_row", "Kurzhantelrudern brustgestützt", "Chest-supported dumbbell row", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("lat_pulldown", "Latziehen", "Lat pulldown", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("face_pull", "Face Pull am Kabel", "Cable face pull", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("triceps_pushdown", "Trizepsdrücken am Kabel", "Triceps pushdown", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("jm_press", "JM Press", "JM press", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("db_lateral_raise", "Seitheben mit Kurzhanteln", "Dumbbell lateral raise", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("hammer_curl", "Hammer-Curl", "Hammer curl", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("back_extension", "Hyperextension 45°", "45° back extension", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("leg_curl", "Beinbeuger an der Maschine", "Machine leg curl", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("ab_wheel", "Ab Wheel Rollout", "Ab wheel rollout", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("hanging_leg_raise", "Hängendes Beinheben", "Hanging leg raise", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("walking_lunge", "Ausfallschritte gehend", "Walking lunge", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("bulgarian_split_squat", "Bulgarische Split-Kniebeuge", "Bulgarian split squat", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("db_shoulder_press", "Kurzhantel-Schulterdrücken", "Dumbbell shoulder press", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("dips", "Dips", "Dips", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("shrug", "Shrugs mit Langhantel", "Barbell shrug", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("leg_press", "Beinpresse", "Leg press", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        // --- P4: further strongman implements and events ---
        e("natural_stone_lift", "Naturstein heben", "Natural stone lift", ExerciseCategory.STONE, true, EventMode.MAX_LOAD),
        e("stone_carry", "Stein tragen", "Stone carry", ExerciseCategory.STONE, true, EventMode.MAX_DISTANCE),
        e("dinnie_stone_hold", "Steinhalten (Dinnie-Stil)", "Stone hold (Dinnie style)", ExerciseCategory.STONE, true, EventMode.MAX_HOLD_TIME),
        e("fingal_fingers", "Fingal's Fingers", "Fingal's fingers", ExerciseCategory.EVENT, true, EventMode.FOR_TIME),
        e("duck_walk", "Duck Walk", "Duck walk", ExerciseCategory.CARRY, true, EventMode.FOR_TIME),
        e("loading_medley", "Lade-Medley", "Loading medley", ExerciseCategory.EVENT, true, EventMode.FOR_TIME),
        e("carry_medley", "Trage-Medley", "Carry medley", ExerciseCategory.CARRY, true, EventMode.FOR_TIME),
        e("anvil_carry", "Amboss tragen", "Anvil carry", ExerciseCategory.CARRY, true, EventMode.MAX_DISTANCE),
        e("crucifix_hold", "Kreuzhalten", "Crucifix hold", ExerciseCategory.EVENT, true, EventMode.MAX_HOLD_TIME),
        e("barrel_press", "Fass drücken", "Barrel press", ExerciseCategory.PRESS, true, EventMode.MAX_REPS),
        e("dumbbell_medley", "Hantel-Medley", "Dumbbell medley", ExerciseCategory.PRESS, true, EventMode.FOR_TIME),
        e("block_press", "Block drücken", "Block press", ExerciseCategory.PRESS, true, EventMode.MAX_LOAD),
        e("axle_deadlift_hold", "Achsen-Griffhalten", "Axle grip hold", ExerciseCategory.EVENT, true, EventMode.MAX_HOLD_TIME),
        e("silver_dollar_deadlift", "Silver-Dollar-Kreuzheben", "Silver dollar deadlift", ExerciseCategory.DEADLIFT, true, EventMode.MAX_LOAD),
        e("deadlift_strict", "Kreuzheben ohne Riemen", "Deadlift without straps", ExerciseCategory.DEADLIFT, false, EventMode.MAX_LOAD),
        e("deadlift_paused", "Kreuzheben mit Pause", "Paused deadlift", ExerciseCategory.DEADLIFT, false, EventMode.MAX_LOAD),
        e("deadlift_block", "Kreuzheben von Blöcken", "Block pull", ExerciseCategory.DEADLIFT, false, EventMode.MAX_LOAD),
        e("rack_pull", "Rack Pull", "Rack pull", ExerciseCategory.DEADLIFT, false, EventMode.MAX_LOAD),
        e("snatch_grip_deadlift", "Reißgriff-Kreuzheben", "Snatch-grip deadlift", ExerciseCategory.DEADLIFT, false, EventMode.MAX_LOAD),
        e("stiff_leg_deadlift", "Gestrecktes Kreuzheben", "Stiff-leg deadlift", ExerciseCategory.DEADLIFT, false, EventMode.MAX_LOAD),
        e("hip_thrust", "Hip Thrust", "Hip thrust", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("belt_squat", "Gürtelkniebeuge", "Belt squat", ExerciseCategory.SQUAT, false, EventMode.MAX_LOAD),
        e("pause_squat", "Kniebeuge mit Pause", "Paused squat", ExerciseCategory.SQUAT, false, EventMode.MAX_LOAD),
        e("pin_squat", "Pin-Kniebeuge", "Pin squat", ExerciseCategory.SQUAT, false, EventMode.MAX_LOAD),
        e("hack_squat", "Hackenschmidt-Kniebeuge", "Hack squat", ExerciseCategory.SQUAT, false, EventMode.MAX_LOAD),
        e("bulgarian_bag_swing", "Sandsackschwung", "Sandbag swing", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("pin_press", "Pin Press", "Pin press", ExerciseCategory.PRESS, false, EventMode.MAX_LOAD),
        e("behind_neck_press", "Nackendrücken", "Behind-the-neck press", ExerciseCategory.PRESS, false, EventMode.MAX_LOAD),
        e("seated_press", "Schulterdrücken sitzend", "Seated overhead press", ExerciseCategory.PRESS, false, EventMode.MAX_LOAD),
        e("log_push_press", "Log Push Press", "Log push press", ExerciseCategory.PRESS, true, EventMode.MAX_LOAD),
        e("log_strict_press", "Log strikt drücken", "Strict log press", ExerciseCategory.PRESS, true, EventMode.MAX_LOAD),
        e("bradford_press", "Bradford Press", "Bradford press", ExerciseCategory.PRESS, false, EventMode.MAX_REPS),
        e("larsen_press", "Larsen Press", "Larsen press", ExerciseCategory.BENCH, false, EventMode.MAX_LOAD),
        e("spoto_press", "Spoto Press", "Spoto press", ExerciseCategory.BENCH, false, EventMode.MAX_LOAD),
        e("board_press", "Brettdrücken", "Board press", ExerciseCategory.BENCH, false, EventMode.MAX_LOAD),
        e("dead_bench", "Dead Bench", "Dead bench", ExerciseCategory.BENCH, false, EventMode.MAX_LOAD),
        e("push_up_weighted", "Liegestütze mit Zusatzgewicht", "Weighted push-up", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("chin_up_weighted", "Klimmzug mit Zusatzgewicht", "Weighted chin-up", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("pendlay_row", "Pendlay-Rudern", "Pendlay row", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("seal_row", "Seal Row", "Seal row", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("t_bar_row", "T-Bar-Rudern", "T-bar row", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("cable_row", "Rudern am Kabel", "Seated cable row", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("meadows_row", "Meadows-Rudern", "Meadows row", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("barbell_curl", "Langhantel-Curl", "Barbell curl", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("preacher_curl", "Scottcurl", "Preacher curl", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("reverse_curl", "Umgekehrter Curl", "Reverse curl", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("skullcrusher", "Stirndrücken", "Skullcrusher", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("overhead_triceps", "Trizepsdrücken über Kopf", "Overhead triceps extension", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("front_raise_plate", "Frontheben mit Scheibe", "Plate front raise", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("rear_delt_fly", "Reverse Butterfly", "Rear delt fly", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("upright_row", "Aufrechtes Rudern", "Upright row", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("neck_harness", "Nackengeschirr", "Neck harness", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("wrist_roller", "Unterarmroller", "Wrist roller", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("plate_pinch", "Scheibenkneifen", "Plate pinch hold", ExerciseCategory.ACCESSORY, true, EventMode.MAX_HOLD_TIME),
        e("captains_of_crush", "Griffzange", "Grip gripper", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("thick_bar_hold", "Dickes-Eisen-Halten", "Thick bar hold", ExerciseCategory.ACCESSORY, true, EventMode.MAX_HOLD_TIME),
        e("farmers_hold", "Farmers-Halten", "Farmer's hold", ExerciseCategory.ACCESSORY, true, EventMode.MAX_HOLD_TIME),
        e("suitcase_carry", "Koffertragen", "Suitcase carry", ExerciseCategory.CARRY, true, EventMode.MAX_DISTANCE),
        e("front_rack_carry", "Front-Rack-Tragen", "Front rack carry", ExerciseCategory.CARRY, true, EventMode.MAX_DISTANCE),
        e("overhead_carry", "Überkopftragen", "Overhead carry", ExerciseCategory.CARRY, true, EventMode.MAX_DISTANCE),
        e("sandbag_shoulder", "Sandsack auf die Schulter", "Sandbag to shoulder", ExerciseCategory.EVENT, true, EventMode.MAX_REPS),
        e("prowler_sprint", "Prowler-Sprint", "Prowler sprint", ExerciseCategory.EVENT, true, EventMode.FOR_TIME),
        e("battle_rope", "Battle Rope", "Battle rope", ExerciseCategory.ACCESSORY, false, EventMode.MAX_HOLD_TIME),
        e("assault_bike", "Air Bike", "Assault bike", ExerciseCategory.ACCESSORY, false, EventMode.MAX_HOLD_TIME),
        e("rower_erg", "Ruderergometer", "Rowing erg", ExerciseCategory.ACCESSORY, false, EventMode.FOR_TIME),
        e("copenhagen_plank", "Copenhagen-Plank", "Copenhagen plank", ExerciseCategory.ACCESSORY, false, EventMode.MAX_HOLD_TIME),
        e("side_plank", "Seitstütz", "Side plank", ExerciseCategory.ACCESSORY, false, EventMode.MAX_HOLD_TIME),
        e("pallof_press", "Pallof Press", "Pallof press", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("suitcase_deadlift", "Koffer-Kreuzheben", "Suitcase deadlift", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("calf_raise", "Wadenheben", "Calf raise", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("tibialis_raise", "Schienbeinheben", "Tibialis raise", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("reverse_hyper", "Reverse Hyper", "Reverse hyper", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("nordic_curl", "Nordic Curl", "Nordic hamstring curl", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("glute_ham_raise", "Glute-Ham-Raise", "Glute-ham raise", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("step_up", "Step-up", "Step-up", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("goblet_squat", "Goblet-Kniebeuge", "Goblet squat", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("kettlebell_swing", "Kettlebell-Schwung", "Kettlebell swing", ExerciseCategory.ACCESSORY, false, EventMode.MAX_REPS),
        e("turkish_getup", "Türkisches Aufstehen", "Turkish get-up", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("jefferson_curl", "Jefferson Curl", "Jefferson curl", ExerciseCategory.ACCESSORY, false, EventMode.MAX_LOAD),
        e("farmers_deadlift", "Farmers-Griffe heben", "Farmer's handle deadlift", ExerciseCategory.DEADLIFT, true, EventMode.MAX_LOAD)
    )
}
