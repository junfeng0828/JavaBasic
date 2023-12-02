package stream;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;

public class CustomCollector implements Collector<CustomCollector.Student, AtomicInteger, Integer> {

    private ToIntFunction<Student> mapper;

    public CustomCollector(ToIntFunction<Student> mapper) {
        this.mapper = mapper;
    }

    @Override
    public Supplier<AtomicInteger> supplier() {
        // Responsible for the gradual collection of the final result,
        // here return new AtomicInteger(0), subsequent accumulation on the basis of this
        return () -> new AtomicInteger(0);
    }

    @Override
    public BiConsumer<AtomicInteger, Student> accumulator() {
        // 每个元素进入后的处理策略
        return (result, current) -> {
            int intValue = mapper.applyAsInt(current);
            if (intValue >= 90) {
                result.addAndGet(3);
            } else if (intValue >= 80) {
                result.addAndGet(2);
            } else if (intValue >= 60) {
                result.addAndGet(1);
            }
        };
    }

    @Override
    public BinaryOperator<AtomicInteger> combiner() {
        // Handling of multiple segmented results, direct summation
        return (result1, result2) -> {
            result1.addAndGet(result2.get());
            return result1;
        };
    }

    @Override
    public Function<AtomicInteger, Integer> finisher() {
        return AtomicInteger::get;
    }

    @Override
    public Set<Characteristics> characteristics() {
        Set<Characteristics> characteristics = new HashSet<>();
        characteristics.add(Characteristics.CONCURRENT);
        characteristics.add(Characteristics.UNORDERED);
        // characteristics.add(Characteristics.IDENTITY_FINISH);
        return characteristics;
    }

    @Getter
    @AllArgsConstructor
    public static class Student {
        private String department;
        private String name;
        private Integer age;
        private String sex;
        private Integer score;

        @Override
        public String toString() {
            return "Student{name='" + name
                    + "', department='" + department
                    + "', age=" + age
                    + "', sex='" + sex
                    + "', score='" + score
                    + "'}";
        }
    }

    public static void main(String[] args) {
        List<Student> allStudents = Lists.newArrayList(
                new Student("Computer", "Bob", 18, "male", 59),
                new Student("Computer", "Ted", 17, "male", 60),
                new Student("Economics", "Zeka", 19, "male", 70),
                new Student("Economics", "Alan", 19, "male", 90),
                new Student( "Computer", "Anne", 21, "female", 80),
                new Student("Physics", "Davis", 21, "female", 99)
        );
        System.out.println("Serial processing results: " + getScoreByCollect(allStudents));
        System.out.println("Parallel processing results: " + getScoreByParallelCollect(allStudents));
    }

    public static Integer getScoreByCollect(List<Student> allStudents) {
        return allStudents.stream().collect(new CustomCollector(Student::getScore));
    }

    public static Integer getScoreByParallelCollect(List<Student> allStudents) {
        return allStudents.stream().collect(new CustomCollector(Student::getScore));
    }
}
