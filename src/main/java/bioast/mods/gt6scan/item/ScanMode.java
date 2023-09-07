package bioast.mods.gt6scan.item;

import gregapi.oredict.OreDictPrefix;

enum ScanMode {
    LARGE(gregapi.data.OP.ore), SMALL(gregapi.data.OP.oreSmall), DENSE_AND_NORMAL(gregapi.data.OP.oreDense), BEDROCK(gregapi.data.OP.oreBedrock), FLUID_BEDROCK(gregapi.data.OP.bucket), ROCK(gregapi.data.OP.rockGt),FLUID(gregapi.data.OP.bucket);
    public final OreDictPrefix OP;

    ScanMode(OreDictPrefix op) {
        OP = op;
    }

    public boolean isTE() {
        switch (this) {
            case LARGE -> {
                return true;
            }
            case SMALL -> {
                return true;
            }
            case DENSE_AND_NORMAL -> {
                return false;
            }
            case BEDROCK -> {
                return true;
            }
            case FLUID_BEDROCK -> {
                return true;
            }
            case ROCK -> {
                return true;
            }
            case FLUID -> {
                return false;
            }
            default -> throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}
