'''
CI Build

@author: J Sittiwerapong
'''

import sys;
import os;
import shutil;
import datetime;
import getpass;
import re;
import tarfile;
from distutils.dir_util import copy_tree

'''
option -l for label
option -v for version
option -e for environment

It supports the app structure like below:

AppName
    -application
        -app_tmp
        -cache
        -config
        -controllers
        -core
        -errors
        ...
    -build
        dev.application_config.php
        dev.database.php
        dev.htaccess
        dev.properties
        prod.applciation_config.php
        prod.database.php
        prod.htaccess
        prod.properties
    -pub
    -system
    .htaccess
    index.php
'''
program_args = sys.argv[1:];
'make program arguments to map'
input_dict = dict(zip([val for idx, val in enumerate(program_args) if idx % 2 == 0], 
                      [val for idx, val in enumerate(program_args) if idx % 2 != 0]));
                      
project_name = "AppName";
version = input_dict['-v'] if '-v' in input_dict else '1.0.0';
env = input_dict['-e'] if '-e' in input_dict else 'dev';
label = input_dict['-l'] if '-l' in input_dict else project_name + '_' + version;
project_dir = input_dict['-d'] if '-d' in input_dict else os.getcwd();
application_dir = project_dir + os.sep + "application"
application_config_dir = application_dir + os.sep + "config";
pub_dir = project_dir + os.sep + "pub"
system_dir = project_dir + os.sep + "system"
build_dir = project_dir + os.sep + "build"
output_dir = project_dir + os.sep + "output";
output_application_dir = output_dir + os.sep + "application";
output_pub_dir = output_dir + os.sep + "pub";
output_system_dir = output_dir + os.sep + "system";
ouput_application_config_dir = output_application_dir + os.sep + "config";




def build(argv=sys.argv):
    cleanup_project(output_dir);
    make_manifest("info");
    properties_map = parse_properties_to_map(build_dir + os.sep + env + ".properties");
    print(properties_map);
    
    try:
        copy_tree(application_dir, output_application_dir);
        copy_tree(pub_dir, output_pub_dir);
        copy_tree(system_dir, output_system_dir);
        copy_config_file(build_dir + os.sep + env + ".htaccess", 
                         output_dir + os.sep + ".htaccess");
        copy_config_file(build_dir + os.sep + env + ".application_config.php", 
                         ouput_application_config_dir + os.sep + "application_config.php", properties_map);
        copy_config_file(application_config_dir + os.sep + "config.php",
                         ouput_application_config_dir + os.sep + "config.php", properties_map);
        copy_config_file(build_dir + os.sep + env + ".database.php", 
                         ouput_application_config_dir + os.sep + "database.php");
        copy_config_file(project_dir + os.sep + "index.php", 
                         output_dir + os.sep + "index.php");
        tarball(project_name + ".tar.gz", output_dir + os.sep);
        print("build sucessful");
    except Exception as e:
        print(e);
            

def cleanup_project(removed_dir):
    print("Cleanup directory...");
    if not os.path.exists(removed_dir):
        os.makedirs(removed_dir);
    
    for the_file in os.listdir(removed_dir):
        file_path = os.path.join(removed_dir, the_file)
        try:
            if os.path.isfile(file_path):
                os.unlink(file_path)
            elif os.path.isdir(file_path): shutil.rmtree(file_path)
        except Exception as e:
            print(e)


def make_manifest(path=""):
    print("Making manifest...");
    base_manifest_dir = output_dir + os.sep + path;
    if not os.path.exists(base_manifest_dir):
        os.makedirs(base_manifest_dir)
    info_str = 'Manifest-Version:' + version + "\n";
    info_str += 'Build-Mode:' + env + "\n";
    info_str += 'Build-Label:' + label + "\n";
    info_str += "Build-Time:" + datetime.datetime.now().strftime('%Y-%m-%d %H:%M') + "\n";
    info_str += "Build-By:" + getpass.getuser();
    print("###\n" + info_str);
    with open(base_manifest_dir + os.sep + 'manifest.mf','w') as f:
        f.write(info_str);
    print("###");
    
    
def parse_properties_to_map(properties_file):
    if not os.path.isfile(properties_file):
        return {};
    with open(properties_file) as f:
        contents = f.readlines();
    config_lines = [line for line in contents if not re.match(r'//', line)];
    
    properties_map = {};
    for cfg_line in config_lines:
        lines= cfg_line.split("=");
        properties_map[lines[0]] = lines[1];
    return properties_map;


def extract_key_value(line):
    match = re.match(r".*config\['(.*)'\]", line, re.M|re.I);
    if match:
        return match.group(1);
    else:
        return "";


def copy_config_file(src_config_file, dest_config_file, filter_map={}):
    print("Copying file from " + src_config_file + " to " + dest_config_file);
    if filter_map:
        with open(src_config_file) as f:
            contents = f.readlines();
        updated_content = "";
        for line in contents:
            key = extract_key_value(line);
            if key and key in filter_map:
                updated_content += "$config['" + key +"'] = '" + filter_map[key] + "';\n";
            else:
                updated_content += line;
        
        with open(dest_config_file,'w') as f:
            f.write(updated_content);
    else:
        shutil.copy2(src_config_file, dest_config_file);
        

def tarball(output_filename, source_dir):
    with tarfile.open(output_filename, "w:gz") as tar:
        tar.add(source_dir, arcname=os.path.basename(source_dir))


if __name__ == "__main__":
    build()
