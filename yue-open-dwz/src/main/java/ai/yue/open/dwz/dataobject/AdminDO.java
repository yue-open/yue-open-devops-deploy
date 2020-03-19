package ai.yue.open.dwz.dataobject;

import ai.yue.library.data.jdbc.dataobject.BaseSnakeCaseDO;
import ai.yue.open.dwz.constant.AdminStatusEnum;
import ai.yue.open.dwz.constant.RoleEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author	ylyue
 * @since	2019年12月2日
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AdminDO extends BaseSnakeCaseDO {

	private static final long serialVersionUID = 1388871813984194034L;
	
	Long user_id;
	String username;
	RoleEnum role;
	AdminStatusEnum admin_status;
	
}
