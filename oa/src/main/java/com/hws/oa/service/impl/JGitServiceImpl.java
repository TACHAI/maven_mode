package com.hws.oa.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.hws.oa.core.LoadConf;
import com.hws.oa.core.threadpool.MyThreadPoolExecutor;
import com.hws.oa.core.threadpool.UpdateTask;
import com.hws.oa.core.websocket.WebSocketServer;
import com.hws.oa.exception.CommonException;
import com.hws.oa.model.SystemModel;
import com.hws.oa.service.JGitService;

@Service
public class JGitServiceImpl implements JGitService{

	private static final String GITFILENAME=".git";
	   
	@Autowired
	WebSocketServer webSocketServer;
	
    /**
     * 完全覆盖本地代码
     * @throws GitAPIException 
     * @throws CommonException 
     * @throws IOException 
     * @throws TransportException 
     * @throws InvalidRemoteException 
     */
	@Override
    public  JSONObject update(Integer num,String jessionId) throws GitAPIException, CommonException, IOException {
    	//step0 检查是否已经初始化
    	List<SystemModel> listSet = LoadConf.getSystems();
    	String remoteRepo = null;
    	String localRepo = null;
    	JSONObject obj = new JSONObject();
    	obj.put("updateFlag", false);
    	if(null != listSet&&listSet.size()>0){
    		SystemModel model = listSet.get(num);
    		if(null == model)return obj;
			remoteRepo = model.getRemoteRepo();
			localRepo = model.getLocalRepo();
		}
    	String[] temp = remoteRepo.split("/");
    	String repoName = temp[temp.length-1].split("\\.")[0];
    	File repo = new File(localRepo+File.separator+repoName);
    	if(!repo.exists())repo.mkdirs();
    	if(repo.isDirectory()){
    		if(!Arrays.asList(repo.list()).contains(GITFILENAME))initGitRepo(remoteRepo,repo.getPath());
		
			String localRepoGitConfig = repo.getPath() + File.separator + GITFILENAME;
			Git git = Git.open(new File(localRepoGitConfig));
			Repository repository = git.getRepository();
			//step1 git fetch --all 
	    	//step2 git reset --hard origin/master
	    	//step3 git pull
			git.fetch().call();
			long time = System.currentTimeMillis();
			Ref local = repository.findRef("refs/heads/master");
			Ref origin = repository.findRef("refs/remotes/origin/master");
			ObjectReader reader = repository.newObjectReader();
			CanonicalTreeParser oldTree = new CanonicalTreeParser();
			CanonicalTreeParser newTree = new CanonicalTreeParser();
			oldTree.reset(reader,repository.resolve(local.getObjectId().name()+"^{tree}"));
			newTree.reset(reader, repository.resolve(origin.getObjectId().name()+"^{tree}"));
			List<DiffEntry> list = git.diff().setOldTree(oldTree).setNewTree(newTree).call();
			if(null == list || list.size() == 0) {
				obj.put("hasDiff",false);
				return obj;
			}
			obj.put("updateFlag", true);
			//当jessionId 为空不执行线程池
			if(null != jessionId&&!"".equals(jessionId)){
				ThreadPoolExecutor executor = MyThreadPoolExecutor.myThreadPoolExecutor.getThreadPoolExecutor();
				executor.submit(new UpdateTask(list,git,time,WebSocketServer.getMapCache().get(jessionId)));
			}
			git.reset().setMode(ResetType.HARD).call();
			git.pull().call();
			
    	}
    	return obj;
    }
    
    /**
     * 初始化git仓库
     * @param num
     * @throws GitAPIException
     * @throws CommonException 
     */
    public  void initGitRepo(String remoteRepo,String localRepo) throws GitAPIException, CommonException{
	    if(null == remoteRepo || null == localRepo)throw new CommonException("remoteRepo/localRepo canot be null[initGitRepo]");
    	Git.cloneRepository().setURI(remoteRepo).setDirectory(new File(localRepo)).call();
    }

	@Override
	public String update(Integer num)
			throws CommonException, IOException, InvalidRemoteException, TransportException, GitAPIException {
		//step0 检查是否已经初始化
    	List<SystemModel> listSet = LoadConf.getSystems();
    	String remoteRepo = null;
    	String localRepo = null;
    	if(null != listSet&&listSet.size()>0){
    		SystemModel model = listSet.get(num);
    		if(null == model)return null;
			remoteRepo = model.getRemoteRepo();
			localRepo = model.getLocalRepo();
		}
    	String[] temp = remoteRepo.split("/");
    	String repoName = temp[temp.length-1].split("\\.")[0];
    	File repo = new File(localRepo+File.separator+repoName);
    	if(!repo.exists())repo.mkdirs();
    	String message="";
    	if(repo.isDirectory()){
    		if(!Arrays.asList(repo.list()).contains(GITFILENAME))initGitRepo(remoteRepo,repo.getPath());
		
			String localRepoGitConfig = repo.getPath() + File.separator + GITFILENAME;
			Git git = Git.open(new File(localRepoGitConfig));
			Repository repository = git.getRepository();
			//step1 git fetch --all 
	    	//step2 git reset --hard origin/master
	    	//step3 git pull
			git.fetch().call();
			Ref local = repository.findRef("refs/heads/master");
			Ref origin = repository.findRef("refs/remotes/origin/master");
			ObjectReader reader = repository.newObjectReader();
			CanonicalTreeParser oldTree = new CanonicalTreeParser();
			CanonicalTreeParser newTree = new CanonicalTreeParser();
			oldTree.reset(reader,repository.resolve(local.getObjectId().name()+"^{tree}"));
			newTree.reset(reader, repository.resolve(origin.getObjectId().name()+"^{tree}"));
			List<DiffEntry> list = git.diff().setOldTree(oldTree).setNewTree(newTree).call();
			if(null == list || list.size() == 0) return null;
			git.reset().setMode(ResetType.HARD).call();
			git.pull().call();
			for(DiffEntry diff:list){
				message += diff.getChangeType() +"      "+diff.getNewPath()+"<br/>";
			}
			
    	}
    	return message;
	}

}
