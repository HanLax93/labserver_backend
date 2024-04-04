package site.handglove.labserver.utils;

import java.io.FileInputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.yaml.snakeyaml.Yaml;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import site.handglove.labserver.model.Container;
import site.handglove.labserver.model.Menu;
import site.handglove.labserver.model.MetaVo;
import site.handglove.labserver.model.RouterVo;

@Repository
public class Helper {
    private static String dockerRoot;

    @Value("${dockerRoot}")
    public void setDockerRoot(String dockerRoot) {
        Helper.dockerRoot = dockerRoot;
    }

    public static OffsetDateTime dateConverter(String date) {
        date = date.substring(0, date.lastIndexOf(" "));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
        OffsetDateTime dateTime = OffsetDateTime.parse(date, formatter);
        return dateTime;
    }

    public static Integer portParser(String ports) {
        Integer port = Integer.valueOf(ports.substring(ports.indexOf(":") + 1, ports.indexOf("-")));
        return port;
    }

    public static Container containerParser(String[] info) {
        var container = new Container();
        container.setDockerID(info[0]);
        container.setCreateTime(Helper.dateConverter(info[3]));
        container.setRunning(info[4].contains("Up") ? 1 : 0);
        if (container.getRunning() == 1) {
            container.setPort(Helper.portParser(info[5]));
            container.setStuIndex(container.getPort() / 1000);
        }
        container.setName(info[6]);

        if (container.getRunning() == 1) {
            ArrayList<String> sshStatusString = ShellCommandRunner.run("sudo docker exec -uroot " + container.getName() + " service ssh status");
            if (sshStatusString.size() != 0 && !sshStatusString.get(0).contains("not")) {
                container.setSshStatus(1);
            }
        }
        return container;
    }

    public static String runContainer(String username) {
        ArrayList<String> commnadResult = ShellCommandRunner.run("sudo docker start " + username);
        return commnadResult.get(0);
    }

    @SuppressWarnings("unchecked")
    public static boolean addService(String name, Integer stuIndex) {
        String configPath = dockerRoot + "stus/wxl2080Ti.yml";
        String servicePath = dockerRoot + "stus/service_base.yml";

        Yaml yaml = new Yaml();
        try (FileInputStream fileInputStream = new FileInputStream(configPath)) {
            Map<String, Object> config = yaml.load(fileInputStream);
            Map<String, Object> services = (Map<String, Object>)config.get("services");
            if (!services.containsKey(name)) {
                ShellCommandRunner.run("cp " + servicePath + " " + servicePath + ".new");
                ShellCommandRunner.run("sed -i \'s#${stu_index}#" + stuIndex + "#g\' " + servicePath + ".new" );
                ShellCommandRunner.run("sed -i \'s#${stu_name}#"+ name + "#g\' " + servicePath + ".new" );
                ShellCommandRunner.run("cat " + servicePath + ".new" + " >> " + configPath);
                ShellCommandRunner.run("rm " + servicePath + ".new");
            };
        } catch (Exception e) {
            e.printStackTrace();
        }

        String stuPath = dockerRoot + "stus/data/stu" + stuIndex;
        String confPath = stuPath + "/conf";
        String aptPath = confPath + "/apt";
        String envsPath = stuPath + "/envs";
        String workspacePath = stuPath + "/workspace";
        String scriptsPath = dockerRoot + "/scripts/apt.tar.gz";

        ShellCommandRunner.run("mkdir -p " + aptPath + " " + envsPath + " " + workspacePath);
        ShellCommandRunner.run("sudo docker-compose -f " + configPath + " up -d");
        ShellCommandRunner.run("tar xzf " + scriptsPath + " -C " + aptPath + " --strip-components 1");
        // check
        List<String> addingResult = containerInfo(name);
        ShellCommandRunner.run("sudo docker exec -uroot " + name + " chown -R stu:sudo /home/stu");
        ShellCommandRunner.run("sudo docker exec -uroot " + name + " service ssh start");
        return addingResult.size() != 0;
        // System.out.println("adding complete.");
    }

    public static List<String> containerInfo(@Nullable String name) {
        String allContainersCommand = "sudo docker ps -a";
        String formatOutput = " --format \"{{.ID}}\\t{{.Image}}\\t{{.Command}}\\t{{.CreatedAt}}\\t{{.Status}}\\t{{.Ports}}\\t{{.Names}}\"";

        if (name != null) {
            formatOutput = formatOutput + " | grep -w " + name;
        }
        ArrayList<String> containerInfo =  ShellCommandRunner.run(allContainersCommand + formatOutput);
        if (containerInfo != null && containerInfo.size() != 0) {
            List<String> stuContainerInfo = containerInfo.stream().filter(item -> item.contains("2080ti")).collect(Collectors.toList());
            return stuContainerInfo;
        }
        return null;
    }

    public static String runContainerSSH(String name) {
        ArrayList<String> commnadResult = ShellCommandRunner.run("sudo docker exec -uroot " + name + " service ssh start");
        return commnadResult.get(0);
    }
    public static int[] stringToIntegerArray(String input) {
        input = input.trim();
        input = input.substring(1, input.length() - 1);
        if (input.length() == 0) {
            return new int[0];
        } else {
            String[] parts = input.split(",");
            int[] output = new int[parts.length];

            for (int index = 0; index < parts.length; ++index) {
                String part = parts[index].trim();
                output[index] = Integer.parseInt(part);
            }

            return output;
        }
    }

    public static List<Menu> buildTree(List<Menu> list) {
        List<Menu> tree = new ArrayList<>();

        for (Menu e : list) {
            if (e.getParentId().intValue() == 0) {
                tree.add(getChildren(e, list));
            }
        }
        return tree;
    }

    public static Menu getChildren(Menu menu, List<Menu> list) {
        menu.setChildren(new ArrayList<Menu>());
        Integer curId = menu.getId();

        for (Menu e : list) {
            if (e.getParentId().equals(curId)) {
                menu.getChildren().add(getChildren(e, list));
            }
        }

        return menu; 
    }

    public static String integerArrayToString(int[] nums) {
        return integerArrayToString(nums, nums.length);
    }

    public static List<RouterVo> buildRouter(List<Menu> sysMenuTree) {
        List<RouterVo> res = new ArrayList<>();
        for (Menu menu : sysMenuTree) {
            RouterVo routerVo = new RouterVo();
            routerVo.setHidden(false);    
            routerVo.setAlwaysShow(false);
            routerVo.setPath(getRouterPath(menu));
            routerVo.setComponent(menu.getComponent());
            routerVo.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            List<Menu> children = menu.getChildren();
            if (menu.getType().longValue() == 1) {
                List<Menu> hiddenMenuList = children.stream().filter(item -> !StringUtils.isEmpty(item.getComponent())).collect(Collectors.toList());
                for (Menu hiddenMenu : hiddenMenuList) {
                    RouterVo hiddenRouterVo = new RouterVo();
                    hiddenRouterVo.setHidden(true);
                    hiddenRouterVo.setAlwaysShow(false);
                    hiddenRouterVo.setPath(getRouterPath(hiddenMenu));
                    hiddenRouterVo.setComponent(hiddenMenu.getComponent());
                    hiddenRouterVo.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    res.add(hiddenRouterVo);
                }
            } else {
                // 顶级菜单 和 二级菜单
                if (!CollectionUtils.isEmpty(children)) {
                    if (children.size() > 0) {
                        routerVo.setAlwaysShow(true);
                    }
                    routerVo.setChildren(buildRouter(children));
                }
            }
            res.add(routerVo);
        }
        return res;
    }

    private static String getRouterPath(Menu menu) {
        String routerPath = "/" + menu.getPath();
        if(menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }

    public static String integerArrayToString(int[] nums, int length) {
        if (length == 0) {
            return "[]";
        } else {
            String result = "";

            for (int index = 0; index < length; ++index) {
                int number = nums[index];
                result = result + Integer.toString(number) + ", ";
            }

            return "[" + result.substring(0, result.length() - 2) + "]";
        }
    }

    public static String integerArrayListToString(List<Integer> nums, int length) {
        if (length == 0) {
            return "[]";
        } else {
            String result = "";

            for (int index = 0; index < length; ++index) {
                Integer number = (Integer) nums.get(index);
                result = result + Integer.toString(number.intValue()) + ", ";
            }

            return "[" + result.substring(0, result.length() - 2) + "]";
        }
    }

    public static String integerArrayListToString(List<Integer> nums) {
        return integerArrayListToString(nums, nums.size());
    }

    public static String int2dListToString(List<List<Integer>> nums) {
        StringBuilder sb = new StringBuilder("[");
        Iterator<List<Integer>> iterator = nums.iterator();

        while (iterator.hasNext()) {
            List<Integer> list = iterator.next();
            sb.append(integerArrayListToString(list));
            sb.append(",");
        }

        sb.setCharAt(sb.length() - 1, ']');
        return sb.toString();
    }
}