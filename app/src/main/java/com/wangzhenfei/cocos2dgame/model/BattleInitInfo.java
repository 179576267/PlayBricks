package com.wangzhenfei.cocos2dgame.model;

import java.util.List;

/**
 * Created by wangzhenfei on 2016/11/11.
 */
public class BattleInitInfo {


    /**
     * id : 1013
     * name : hoodle_1013
     * avatar : hoodle_1013
     * propList : [{"id":12,"blockId":112,"type":1},{"id":11,"blockId":111,"type":2},{"id":9,"blockId":109,"type":3}]
     * blockList : [{"id":101,"type":2},{"id":102,"type":2},{"id":103,"type":1},{"id":104,"type":1},{"id":105,"type":2},{"id":106,"type":2},{"id":107,"type":2},{"id":108,"type":1},{"id":109,"type":2},{"id":110,"type":1},{"id":111,"type":2},{"id":112,"type":1},{"id":113,"type":2},{"id":114,"type":2},{"id":100,"type":0}]
     */

    private InitBatterBean initiativeUser;
    /**
     * id : 1012
     * name : hoodle_1012
     * avatar : hoodle_1012
     * propList : [{"id":11,"blockId":211,"type":1},{"id":1,"blockId":201,"type":2},{"id":12,"blockId":212,"type":3}]
     * blockList : [{"id":201,"type":2},{"id":202,"type":2},{"id":203,"type":1},{"id":204,"type":1},{"id":205,"type":2},{"id":206,"type":2},{"id":207,"type":2},{"id":208,"type":1},{"id":209,"type":1},{"id":210,"type":1},{"id":211,"type":1},{"id":212,"type":1},{"id":213,"type":2},{"id":214,"type":1},{"id":200,"type":0}]
     */

    private InitBatterBean passivityUser;

    public InitBatterBean getInitiativeUser() {
        return initiativeUser;
    }

    public void setInitiativeUser(InitBatterBean initiativeUser) {
        this.initiativeUser = initiativeUser;
    }

    public InitBatterBean getPassivityUser() {
        return passivityUser;
    }

    public void setPassivityUser(InitBatterBean passivityUser) {
        this.passivityUser = passivityUser;
    }

    public static class InitBatterBean {
        private int id;
        private String name;
        private String avatar;
        /**
         * id : 12
         * blockId : 112
         * type : 1
         */

        private List<PropListBean> propList;
        /**
         * id : 101
         * type : 2
         */

        private List<BlockListBean> blockList;

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

        public List<PropListBean> getPropList() {
            return propList;
        }

        public void setPropList(List<PropListBean> propList) {
            this.propList = propList;
        }

        public List<BlockListBean> getBlockList() {
            return blockList;
        }

        public void setBlockList(List<BlockListBean> blockList) {
            this.blockList = blockList;
        }

        public static class PropListBean {
            private int id;
            private int blockId;
            private int type;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public int getBlockId() {
                return blockId;
            }

            public void setBlockId(int blockId) {
                this.blockId = blockId;
            }

            public int getType() {
                return type;
            }

            public void setType(int type) {
                this.type = type;
            }
        }

        public static class BlockListBean {
            private int id;
            private int type;

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
        }
    }
}
