/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vd.DiskScaner.Files;

import java.util.List;

/**
 *
 * @author Kharisov Ruslan
 */
public interface FindFiles {

    List<String> getDisks();

    FileInfo startwithdisk(String drive);

}
