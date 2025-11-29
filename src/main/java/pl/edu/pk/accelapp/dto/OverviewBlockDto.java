package pl.edu.pk.accelapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class OverviewBlockDto {
    private final double timeSec;
    private final List<GroupStats> groups;

    public double getTimeSec() {
        return timeSec;
    }

    public List<GroupStats> getGroups() {
        return groups;
    }

    public static class GroupStats {
        private final int groupIndex;
        private final List<Integer> channels;
        private final double min;
        private final double max;
        private final double mean;

        public GroupStats(int groupIndex,
                          List<Integer> channels,
                          double min,
                          double max,
                          double mean) {
            this.groupIndex = groupIndex;
            this.channels = channels;
            this.min = min;
            this.max = max;
            this.mean = mean;
        }

        public int getGroupIndex() {
            return groupIndex;
        }

        public List<Integer> getChannels() {
            return channels;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        public double getMean() {
            return mean;
        }
    }
}
