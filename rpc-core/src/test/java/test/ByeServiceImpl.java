package test;

import com.szq.rpc.annotation.Service;
import com.szq.rpc.api.ByeService;


/**
 * @author Ashur
 * @description 服务实现类
 */
@Service
public class ByeServiceImpl implements ByeService {

    @Override
    public String bye(String name) {
        return "bye," + name;
    }

}