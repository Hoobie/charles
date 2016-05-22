package pl.joegreen.charles.model.topology;

/**
 * Created by FleenMobile on 2016-05-22.
 */
public class TopologyFactory {

    public static PopulationsTopology getTopology(String code) {
        switch (code) {
            case "FULL_MESH":
                return new FullMeshPopulationsTopology();
            case "RING":
                return new RingPopulationsTopology();
            case "STAR":
                return new StarPopulationsTopology();
            default:
                return new FullMeshPopulationsTopology();
        }
    }
}
