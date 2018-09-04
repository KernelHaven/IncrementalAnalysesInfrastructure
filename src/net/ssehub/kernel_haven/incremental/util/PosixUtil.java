package net.ssehub.kernel_haven.incremental.util;

import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class that offers functions for working with POSIX file permissions.
 * @author Moritz
 */
public class PosixUtil {

    /**
     * Block constructor for Util class from public access.
     * 
     * @author Moritz
     */
    private PosixUtil() {

    }

    /**
     * Gets the posix file permission for the number representation of the
     * permission.
     *
     * @param numberString the number string
     * @return the posix file permission for number string
     */
    public static Set<PosixFilePermission> getPosixFilePermissionForNumberString(String numberString) {
        Set<PosixFilePermission> perms = new HashSet<>();

        if (numberString != null && numberString.length() == 3) {
            numberString = "0" + numberString;
        }

        if (numberString != null && numberString.length() == 4 && numberString.matches("\\d*")) {
            handleOwnerRights(numberString, perms);
            handleGroupRights(numberString, perms);
            handleOthersRights(numberString, perms);
        }

        return perms;

    }

    /**
     * Determines file permissions for others and adds them to the list of
     * permissions.
     *
     * @param numberString the number string
     * @param perms        the perms
     */
    private static void handleOthersRights(String numberString, Set<PosixFilePermission> perms) {
        switch (numberString.charAt(3)) {
        case '1':
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
            break;
        case '2':
            perms.add(PosixFilePermission.OTHERS_WRITE);
            break;
        case '3':
            perms.add(PosixFilePermission.OTHERS_WRITE);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
            break;
        case '4':
            perms.add(PosixFilePermission.OTHERS_READ);
            break;
        case '5':
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
            break;
        case '6':
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_WRITE);
            break;
        case '7':
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_WRITE);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
            break;
        default:
            break;
        }
    }

    /**
     * Determines file permissions for group and adds them to the list of
     * permissions.
     *
     * @param numberString the number string
     * @param perms        the perms
     */
    private static void handleGroupRights(String numberString, Set<PosixFilePermission> perms) {
        switch (numberString.charAt(2)) {
        case '1':
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            break;
        case '2':
            perms.add(PosixFilePermission.GROUP_WRITE);
            break;
        case '3':
            perms.add(PosixFilePermission.GROUP_WRITE);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            break;
        case '4':
            perms.add(PosixFilePermission.GROUP_READ);
            break;
        case '5':
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            break;
        case '6':
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_WRITE);
            break;
        case '7':
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_WRITE);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            break;
        default:
            break;
        }
    }

    /**
     * Determines file permissions for owner and adds them to the list of
     * permissions.
     *
     * @param numberString the number string
     * @param perms        the perms
     */
    private static void handleOwnerRights(String numberString, Set<PosixFilePermission> perms) {
        switch (numberString.charAt(1)) {
        case '1':
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            break;
        case '2':
            perms.add(PosixFilePermission.OWNER_WRITE);
            break;
        case '3':
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            break;
        case '4':
            perms.add(PosixFilePermission.OWNER_READ);
            break;
        case '5':
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            break;
        case '6':
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            break;
        case '7':
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            break;
        default:
            break;
        }
    }

}
