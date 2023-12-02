package stream;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import utils.JsonUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamCase {

    @Getter
    @AllArgsConstructor
    public static class Student {
        private String name;
        private Integer age;
    }

    @Getter
    @AllArgsConstructor
    public static class Team {
        private String type;
        private List<Student> students;
    }

    public static void main(String[] args) {

        List<Student> students = Lists.newArrayList(
                new Student("Bob", 18),
                new Student("Ted", 17),
                new Student("Zeka", 19));

        StreamCase streamCase = new StreamCase();
        List<Student> twoOldestStudents = streamCase.getTwoOldestStudents(students);
        System.out.println(JsonUtils.toJson(twoOldestStudents));

        List<Student> twoOldestStudentsByStream = streamCase.getTwoOldestStudentsByStream(students);
        System.out.println(JsonUtils.toJson(twoOldestStudentsByStream));

        System.out.println(streamCase.objectToString(students));


        List<Student> basketballStudents = Lists.newArrayList(
                new Student("Bob", 18),
                new Student("Ted", 17),
                new Student("Zeka", 19),
                new Student("Tom", 19)    //some case
        );

        List<Student> footballStudent = Lists.newArrayList(
                new Student("Alan", 19),
                new Student("Anne", 21),
                new Student("Davis", 21));

        Team basketballTeam = new Team("bastetball", basketballStudents);
        Team footballTeam = new Team("football", footballStudent);
        List<Team> teams = Lists.newArrayList(basketballTeam, footballTeam);


        List<Student> allStudents = teams.stream()
                .flatMap(t -> t.getStudents().stream())
                .collect(Collectors.toList());
        System.out.println("allStudents: " + JsonUtils.toJson(allStudents));

        System.out.println("------start peek------");
        basketballTeam.getStudents().stream().peek(s -> System.out.println("Hello, " + s.getName()));
        System.out.println("------end peek------");
        System.out.println();
        System.out.println("------start foreach------");
        basketballTeam.getStudents().stream().forEach(s -> System.out.println("Hello, " + s.getName()));
        System.out.println("------end foreach------");

        System.out.println();
        System.out.println("------start peek------");
        basketballTeam.getStudents().stream().peek(s -> System.out.println("Hello, " + s.getName())).count();
        System.out.println("------end peek------");

        List<Integer> topTwoAges = allStudents.stream()
                .map(Student::getAge)
                .filter(a -> a >= 18)
                .distinct()
                .sorted((a1, a2) -> a2 - a1)
                .skip(1)
//                .limit(2)
                .collect(Collectors.toList());
        System.out.println(topTwoAges);

        footballTeam.getStudents().stream()
                .map(Student::getAge)
                .max(Comparator.comparing(a -> a))
                .ifPresent(a -> System.out.println("The maximum age for a football team is " + a));

        footballTeam.getStudents().stream()
                .map(Student::getAge)
                .min(Comparator.comparing(a -> a))
                .ifPresent(a -> System.out.println("The minimum age for a football team is " + a));

        //findFirst
        basketballStudents.stream()
                .filter(s -> s.getAge() == 19)
                .findFirst()
                .map(Student::getName)
                .ifPresent(name -> System.out.println("findFirst: " + name));

        //findAny
        basketballStudents.stream()
                .filter(s -> s.getAge() == 19)
                .findAny()
                .map(Student::getName)
                .ifPresent(name -> System.out.println("findAny: " + name));

        //count
        System.out.println("The number of students on the basketball team: " + basketballStudents.stream().count());

        //anymatch
        System.out.println("anymatch: "
                + footballStudent.stream().anyMatch(s -> s.getName().equals("Alan")));

        //allmatch
        System.out.println("allmatch: "
                + footballStudent.stream().allMatch(s -> s.getAge() < 22));

        //nonematch
        System.out.println("noneMatch: "
                + footballStudent.stream().noneMatch(s -> s.getAge() > 20));

        //collect
        Set<Integer> ageSet = basketballStudents.stream().map(Student::getAge).collect(Collectors.toSet());
        System.out.println("set: " + ageSet);

        Map<String, Integer> nameAndAgeMap = basketballStudents.stream().collect(Collectors.toMap(Student::getName, Student::getAge));
        System.out.println("map: " + nameAndAgeMap);


        System.out.println(basketballStudents.stream().map(Student::getName).collect(Collectors.joining(",")));

        System.out.println(basketballStudents.stream().map(Student::getName).collect(Collectors.joining(",", "(",
                ")")));
//        String.join(",", );

        //Calculate average
        System.out.println("average age: "
                + basketballStudents.stream().map(Student::getAge).collect(Collectors.averagingInt(a -> a)));

        //Summary statistics
        IntSummaryStatistics summary = basketballStudents.stream()
                .map(Student::getAge)
                .collect(Collectors.summarizingInt(a -> a));
        System.out.println("summary: " + summary);

        Spliterator spliterator = allStudents.spliterator();
        System.out.println();

        System.out.println(JsonUtils.toJson(basketballStudents));

        //
        for (int i = 0; i < 10; i++) {
            basketballStudents.parallelStream()
                    .filter(s -> s.getAge() >= 18)
                    .findAny()
                    .map(Student::getName)
                    .ifPresent(name -> System.out.println("findAny in parallel stream: " + name));
        }

        // tips1
        Stream<Student> studentStream = basketballStudents.stream()
                .filter(s -> s.getAge() == 19)
                .peek(s -> System.out.println(s.getName()));
        System.out.println("--------end--------");

        // tips2
        studentStream = basketballStudents.stream().filter(s -> s.getAge() == 19);
        // Calculate the number of students
        System.out.println(studentStream.count());
        // If you try it again, an error will be reported
        try {
            System.out.println(studentStream.count());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Student> getTwoOldestStudents(List<Student> students) {
        List<Student> result = new ArrayList<>();
        // 1.Loop to determine the age of students, first filter out students who match the age
        for (Student student : students) {
            if (student.getAge() >= 18) {
                result.add(student);
            }
        }
        // 2.Sort the list of eligible students by age
        result.sort((s1, s2) -> s2.getAge() - s1.getAge());
        // 3.Determine the result size. If it is greater than 2,
        // intercept the sublist of the first two data and return it.
        if (result.size() > 2) {
            result = result.subList(0, 2);
        }
        return result;
    }

    public List<Student> getTwoOldestStudentsByStream(List<Student> students) {
        return students.stream()
                .filter(s -> s.getAge() >= 18)
                .sorted((s1, s2) -> s2.getAge() - s1.getAge())
                .limit(2)
                .collect(Collectors.toList());
    }

    /**
     * Use of map: one-to-one
     *
     * @param students
     * @return
     */
    public List<String> objectToString(List<Student> students) {
        return students.stream()
                .map(Student::getName)
                .collect(Collectors.toList());
    }

}
