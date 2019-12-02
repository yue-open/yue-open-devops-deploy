package ai.yue.open.dwz.dataobject;

import ai.yue.library.data.jdbc.dataobject.DBDO;
import ai.yue.open.dwz.constant.AdminStatusEnum;
import ai.yue.open.dwz.constant.RoleEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author	ylyue
 * @since	2019年12月2日
 */
@Data
@EqualsAndHashCode(callSuper = true) 
public class AdminDO extends DBDO {

	Long user_id;
	String username;
	RoleEnum role;
	AdminStatusEnum admin_status;
	
}
