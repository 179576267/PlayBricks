package com.wangzhenfei.cocos2dgame.model;

import java.util.List;

/**
 * Created by wangzhenfei on 2016/11/11.
 */
public class BattleInitInfo {

    /**
     * id : 1004
     * name : hoodle_1004
     * avatar : hoodle_1004
     * blockList : [{"id":101,"type":0,"propType":0},{"id":102,"type":0,"propType":0},{"id":103,"type":0,"propType":0},{"id":104,"type":0,"propType":0},{"id":105,"type":0,"propType":0},{"id":106,"type":0,"propType":0},{"id":107,"type":0,"propType":1},{"id":108,"type":0,"propType":0},{"id":109,"type":0,"propType":3},{"id":110,"type":0,"propType":2},{"id":111,"type":0,"propType":0},{"id":112,"type":0,"propType":0},{"id":113,"type":0,"propType":0},{"id":114,"type":0,"propType":0},{"id":100,"type":0,"propType":0}]
     */

    private InitiativeUserBean initiativeUser;
    /**
     * id : 1002
     * name : hoodle_1002
     * avatar : hoodle_1002
     * blockList : [{"id":201,"type":0,"propType":0},{"id":202,"type":0,"propType":0},{"id":203,"type":0,"propType":0},{"id":204,"type":0,"propType":0},{"id":205,"type":0,"propType":0},{"id":206,"type":0,"propType":0},{"id":207,"type":0,"propType":0},{"id":208,"type":0,"propType":0},{"id":209,"type":0,"propType":0},{"id":210,"type":0,"propType":2},{"id":211,"type":0,"propType":3},{"id":212,"type":0,"propType":0},{"id":213,"type":0,"propType":1},{"id":214,"type":0,"propType":0},{"id":200,"type":0,"propType":0}]
     */

    private InitiativeUserBean passivityUser;

    public InitiativeUserBean getInitiativeUser() {
        return initiativeUser;
    }

    public void setInitiativeUser(InitiativeUserBean initiativeUser) {
        this.initiativeUser = initiativeUser;
    }

    public InitiativeUserBean getPassivityUser() {
        return passivityUser;
    }

    public void setPassivityUser(InitiativeUserBean passivityUser) {
        this.passivityUser = passivityUser;
    }

    public static class InitiativeUserBean {
        private int id;
        private String name;
        private String avatar;
        private String ip;
        /**
         * id : 101
         * type : 0
         * propType : 0
         */

        private List<BlockListBean> blockList;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public List<BlockListBean> getBlockList() {
            return blockList;
        }

        public void setBlockList(List<BlockListBean> blockList) {
            this.blockList = blockList;
        }

        public static class BlockListBean {
            private int id;
            private int type;
            private int propType;

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                BlockListBean that = (BlockListBean) o;

                return id == that.id;

            }

            @Override
            public int hashCode() {
                return id;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public int getType() {
                return type;
            }

            public void setType(int type) {
                this.type = type;
            }

            public int getPropType() {
                return propType;
            }

            public void setPropType(int propType) {
                this.propType = propType;
            }
        }
    }

}
