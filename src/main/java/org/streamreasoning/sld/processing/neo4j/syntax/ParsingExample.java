package org.streamreasoning.sld.processing.neo4j.syntax;

//import org.streamreasoning.rsp4j.yasper.querying.syntax.QueryFactory;

import org.streamreasoning.sld.processing.shared.ContinuousQuery;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


public class ParsingExample {


    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {


        // QUERY STUDENT TRICK TEST

        // TIMESTAMPS IN EPOCH MILLIS:
        //     14.10.2022 14:45 Uhr --> 1665758700000
        //     14.10.2022 15:00 Uhr --> 1665759600000
        //     14.10.2022 15:15 Uhr --> 1665760500000
        //     14.10.2022 15:20 Uhr --> 1665760800000
        //     14.10.2022 15:40 Uhr --> 1665762000000

        try {

            ContinuousQuery studentTrick = (ContinuousQuery) SeraphQueryFactory.parse(
                    "REGISTER QUERY student_trick STARTING AT 2022-10-14T14:45 {\n" +
                    "WITH duration({minutes : 5}) as _5m,\n" +
                    "duration({minutes : 20}) as _20m\n" +
                    "MATCH (s:Station)<-[r1:rentedAt]-(b1:Bike),\n" +
                    "(b1)-[n1:returnedAt]->(p:Station),\n" +
                    "(p)<-[r2:rentedAt]-(b2:Bike),\n" +
                    "(b2)-[n2:returnedAt]->(o:Station)\n" +
                    "WITHIN PT1H\n" +
                    "WITH duration({minutes : 5}) as _5m\n" +
                    "MATCH (s:Station)<-[r1:rentedAt]-(b1:Bike)\n" +
                    "WITHIN PT5M\n" +
                    "WHERE r1.user_id = n1.user_id AND\n" +
                    "n1.user_id = r2.user_id AND r2.user_id = n2.user_id AND\n" +
                    "n1.val_time < r2.val_time AND\n" +
                    "duration.between(n1.val_time,r2.val_time) < _5m AND\n" +
                    "duration.between(r1.val_time,n1.val_time) < _20m AND\n" +
                    "duration.between(r2.val_time,n2.val_time) < _20m\n" +
                    "EMIT r1.user_id, s.id, p.id, o.id\n" +
                    "ON ENTERING\n" +
                    "EVERY PT5M\n" +
                    "}"
                    , "test");


        } catch (RuntimeException e) {
            System.out.println(e);
        }


    }
}

