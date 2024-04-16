import com.BruceHuntJobApplication;
import com.bruce.entity.User;
import com.bruce.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(classes = BruceHuntJobApplication.class)
public class Test4J {
    @Autowired
    private UserMapper userMapper;
    
    @Test
    public void backTrackTest() {
        //        String s = "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z";
//        String s = "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U";
        String s = "A,B";
        String[] split = s.split(",");
        List<String> list = Arrays.asList(split);
        List<List<String>> lists = generatePermutations(list);
        System.out.println("lists size is " + lists.size());
//        System.out.println(lists);
    }
    
    @Test
    public void myBatisPlusTest() {
        List<User> users = userMapper.selectList(null);
        users.forEach(System.out::println);
        System.out.println("mybatis plus better");
        
    }
    
    
    
    
    
    
    
    
    
    
    
    

    public static List<List<String>> generatePermutations(List<String> list) {
        List<List<String>> result = new ArrayList<>();
        backtrack(list, new ArrayList<>(), result);
        return result;
    }

    private static void backtrack(List<String> list, List<String> tempList, List<List<String>> result) {
        if (!tempList.isEmpty()) {
            result.add(new ArrayList<>(tempList));
        }
        for (int i = 0; i < list.size(); i++) {
            tempList.add(list.get(i));
            ArrayList<String> remainingList = new ArrayList<>(list.subList(i + 1, list.size()));
            backtrack(remainingList, tempList, result);
            tempList.remove(tempList.size() - 1);
        }
    }

}
